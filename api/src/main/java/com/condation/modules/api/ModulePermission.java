package com.condation.modules.api;

/*-
 * #%L
 * modules-api
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Permissions that a module can request.
 *
 * @author jules
 */
public enum ModulePermission {
	IO("java.io.", "java.nio."),
	NET("java.net.", "jdk.net.", "sun.net."),
	SQL("java.sql.", "javax.sql."),
	REFLECT("java.lang.reflect.");

	private final List<String> packages;

	ModulePermission(String... packages) {
		this.packages = Arrays.asList(packages);
	}

	public List<String> getPackages() {
		return Collections.unmodifiableList(packages);
	}
}
