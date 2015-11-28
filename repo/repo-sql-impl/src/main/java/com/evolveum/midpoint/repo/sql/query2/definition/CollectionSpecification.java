/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.repo.sql.query2.definition;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.repo.sql.query2.DefinitionSearchResult;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.Holder;

import javax.xml.namespace.QName;

/**
 * In current version this is just a marker class - there's nothing to be said about standard collections yet.
 *
 * @author mederly
 */
public class CollectionSpecification {

    public String toString() {
        return "StdCol";
    }

    public String getShortInfo() {
        return "[]";
    }
}
