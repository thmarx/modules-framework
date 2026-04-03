package com.condation.modules.manager;

/*-
 * #%L
 * modules-manager
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SandboxingTest {

    @Test
    public void testRestrictedAccess() throws Exception {
        ModuleAPIClassLoader apiClassLoader = new ModuleAPIClassLoader(getClass().getClassLoader(), Collections.emptyList());
        ModuledFirstURLClassLoader loader = new ModuledFirstURLClassLoader(new URL[0], apiClassLoader, Collections.emptyList());

        assertThrows(SecurityException.class, () -> {
            loader.loadClass("java.io.File");
        });
    }

    @Test
    public void testAllowedAccessWithPermission() throws Exception {
        ModuleAPIClassLoader apiClassLoader = new ModuleAPIClassLoader(getClass().getClassLoader(), Collections.emptyList());
        ModuledFirstURLClassLoader loader = new ModuledFirstURLClassLoader(new URL[0], apiClassLoader, List.of(ModulePermission.IO));

        Class<?> fileClass = loader.loadClass("java.io.File");
        assertNotNull(fileClass);
    }

    @Test
    public void testAlwaysAllowedAccess() throws Exception {
        ModuleAPIClassLoader apiClassLoader = new ModuleAPIClassLoader(getClass().getClassLoader(), Collections.emptyList());
        ModuledFirstURLClassLoader loader = new ModuledFirstURLClassLoader(new URL[0], apiClassLoader, Collections.emptyList());

        Class<?> stringClass = loader.loadClass("java.lang.String");
        assertNotNull(stringClass);

        Class<?> listClass = loader.loadClass("java.util.ArrayList");
        assertNotNull(listClass);
    }

    @Test
    public void testNetRestricted() throws Exception {
        ModuleAPIClassLoader apiClassLoader = new ModuleAPIClassLoader(getClass().getClassLoader(), Collections.emptyList());
        ModuledFirstURLClassLoader loader = new ModuledFirstURLClassLoader(new URL[0], apiClassLoader, Collections.emptyList());

        assertThrows(SecurityException.class, () -> {
            loader.loadClass("java.net.URL");
        });
    }

    @Test
    public void testNetAllowedWithPermission() throws Exception {
        ModuleAPIClassLoader apiClassLoader = new ModuleAPIClassLoader(getClass().getClassLoader(), Collections.emptyList());
        ModuledFirstURLClassLoader loader = new ModuledFirstURLClassLoader(new URL[0], apiClassLoader, List.of(ModulePermission.NET));

        Class<?> urlClass = loader.loadClass("java.net.URL");
        assertNotNull(urlClass);
    }
}
