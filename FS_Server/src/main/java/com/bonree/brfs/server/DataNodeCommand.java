/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bonree.brfs.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonree.brfs.authentication.SimpleAuthenticationModule;
import com.bonree.brfs.disknode.DataNodeModule;
import com.bonree.email.EmailModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;

import io.airlift.airline.Command;

@Command(
    name = "data",
    description = "Runs a data node"
)
public class DataNodeCommand extends BaseCommand {
    private static final Logger log = LoggerFactory.getLogger(DataNodeCommand.class);
    
    public DataNodeCommand() {
        super(log);
    }

    @Override
    protected List<Module> getModules() {
        return ImmutableList.of(
                new EmailModule(),
                new SimpleAuthenticationModule(),
                new DataNodeModule());
    }

}
