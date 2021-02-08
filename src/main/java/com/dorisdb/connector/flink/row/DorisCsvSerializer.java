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

package com.dorisdb.connector.flink.row;

import java.io.StringWriter;

import org.apache.flink.calcite.shaded.com.google.common.base.Strings;

public class DorisCsvSerializer implements DorisISerializer {
    
    private static final long serialVersionUID = 1L;

    private final String HEX_STRING = "0123456789ABCDEF";

    private final String columnSeparator;

    public DorisCsvSerializer(String sp) {
        this.columnSeparator = parseByteSeparator(sp);
    }

    @Override
    public String serialize(Object[] values) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (Object val : values) {
            sb.append(val);
            if (idx++ < values.length - 1) {
                sb.append(columnSeparator);
            }
        }
        return sb.toString();
    }

    private String parseByteSeparator(String sp) {
        if (Strings.isNullOrEmpty(sp)) {
            // `\t` by default
            return "\t";
        }
        if (!sp.toUpperCase().startsWith("\\X")) {
            return sp;
        }
        String hexStr = sp.substring(2);
        // check hex str
        if (hexStr.isEmpty()) {
            throw new RuntimeException("Failed to parse column_separator: `Hex str is empty`");
        }
        if (hexStr.length() % 2 != 0) {
            throw new RuntimeException("Failed to parse column_separator: `Hex str length error`");
        }
        for (char hexChar : hexStr.toUpperCase().toCharArray()) {
            if (HEX_STRING.indexOf(hexChar) == -1) {
                throw new RuntimeException("Failed to parse column_separator: `Hex str format error`");
            }
        }
        // transform to separator
        StringWriter writer = new StringWriter();
        for (byte b : hexStrToBytes(hexStr)) {
            writer.append((char) b);
        }
        return writer.toString();
    }

    private byte[] hexStrToBytes(String hexStr) {
        String upperHexStr = hexStr.toUpperCase();
        int length = upperHexStr.length() / 2;
        char[] hexChars = upperHexStr.toCharArray();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            bytes[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return bytes;
    }

    private byte charToByte(char c) {
        return (byte) HEX_STRING.indexOf(c);
    }
    
}
