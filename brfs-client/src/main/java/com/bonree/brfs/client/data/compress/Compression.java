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
package com.bonree.brfs.client.data.compress;

public enum Compression {
    NONE(0) {
        
        @Override
        public Compressor compressor() {
            return Compressor.NONE;
        }

        @Override
        public Decompressor decompressor() {
            return Decompressor.NONE;
        }
        
    };
    
    private final int code;
    
    private Compression(int code) {
        this.code = code;
    }
    
    public int code() {
        return code;
    }
    
    public abstract Compressor compressor();
    public abstract Decompressor decompressor();
}