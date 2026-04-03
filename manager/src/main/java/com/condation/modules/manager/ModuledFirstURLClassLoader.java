package com.condation.modules.manager;

/*-
 * #%L
 * modules-manager
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.condation.modules.api.ModulePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * A strict child-first ClassLoader that isolates module dependencies
 * and only delegates to the ModuleAPIClassLoader when explicitly allowed.
 */
public class ModuledFirstURLClassLoader extends URLClassLoader {

    private final ModuleAPIClassLoader moduleAPIClassLoader;
    private final List<ModulePermission> permissions;

    public ModuledFirstURLClassLoader(URL[] classpath, ModuleAPIClassLoader moduleAPIClassLoader) {
        this(classpath, moduleAPIClassLoader, Collections.emptyList());
    }

    public ModuledFirstURLClassLoader(URL[] classpath, ModuleAPIClassLoader moduleAPIClassLoader, List<ModulePermission> permissions) {
        // Use system classloader as parent to avoid unwanted delegation
        super(classpath, ClassLoader.getSystemClassLoader());
        this.moduleAPIClassLoader = moduleAPIClassLoader;
        this.permissions = permissions;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// Check already loaded
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            if (resolve) resolveClass(loadedClass);
            return loadedClass;
        }

        // Check permissions
        checkPermission(name);

		// System and JDK classes → always from parent/system
        if (isSystemClass(name)) {
            return super.loadClass(name, resolve);
        }

		// Try to find class in this module first (child-first)
        try {
            Class<?> clazz = findClass(name);
            if (resolve) resolveClass(clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
			// If explicitly allowed, load from API loader
            if (moduleAPIClassLoader.isAllowed(name)) {
                try {
                    Class<?> clazz = moduleAPIClassLoader.loadClass(name);
                    if (resolve) resolveClass(clazz);
                    return clazz;
                } catch (ClassNotFoundException ignored) {
                    // Fallthrough
                }
            }
			// Fallback: maybe system/parent has it (e.g. JDK or shared lib)
            return super.loadClass(name, resolve);
        }
    }

    private void checkPermission(String name) {
        if (isRestricted(name)) {
            if (!isAllowed(name)) {
                throw new SecurityException("Access to restricted class denied: " + name + ". Module needs appropriate permission.");
            }
        }
    }

    private boolean isRestricted(String name) {
        return name.startsWith("java.io.")
                || name.startsWith("java.nio.")
                || name.startsWith("java.net.")
                || name.startsWith("jdk.net.")
                || name.startsWith("sun.net.")
                || name.startsWith("java.sql.")
                || name.startsWith("javax.sql.")
                || name.startsWith("java.lang.reflect.");
    }

    private boolean isAllowed(String name) {
        for (ModulePermission permission : permissions) {
            for (String pkg : permission.getPackages()) {
                if (name.startsWith(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSystemClass(String name) {
        return name.startsWith("java.")
            || name.startsWith("sun.")
            || name.startsWith("jdk.")
            || name.startsWith("org.w3c.")
            || name.startsWith("org.xml.")
            || name.startsWith("org.objectweb.asm.")
            || name.startsWith("com.sun.");
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) url = super.getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = new ArrayList<>();
        Enumeration<URL> local = findResources(name);
        while (local.hasMoreElements()) urls.add(local.nextElement());

        Enumeration<URL> parent = getParent().getResources(name);
        while (parent.hasMoreElements()) urls.add(parent.nextElement());

        return Collections.enumeration(urls);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        if (url != null) {
            try {
                return url.openStream();
            } catch (IOException ignored) {}
        }
        return null;
    }

    public <T> ServiceLoader<T> loadService(Class<T> serviceClass) {
        return ServiceLoader.load(serviceClass, this);
    }
}
