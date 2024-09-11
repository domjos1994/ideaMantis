/*
 * Copyright (c) 2024 DOMINIC JOAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.domjos.ideaMantis.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dominic Joas on 19.05.2017.
 */
public class CustomFieldResult {
    private CustomField field;
    private final List<String> result;

    public CustomFieldResult() {
        this.field = null;
        this.result = new LinkedList<>();
    }

    public CustomField getField() {
        return field;
    }

    public void setField(CustomField field) {
        this.field = field;
    }

    public List<String> getResult() {
        return result;
    }

    public void addResult(String result) {
        this.result.add(result);
    }
}
