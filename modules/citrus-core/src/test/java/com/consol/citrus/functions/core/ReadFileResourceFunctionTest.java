/*
 * Copyright 2006-2015 the original author or authors.
 *
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

package com.consol.citrus.functions.core;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ReadFileResourceFunctionTest extends AbstractTestNGUnitTest {

    /** Class under test */
    private ReadFileResourceFunction function = new ReadFileResourceFunction();

    @Test
    public void testExecute() throws Exception {
        context.setVariable("user", "Christoph");

        String result = function.execute(Arrays.asList("classpath:com/consol/citrus/functions/file.txt"), context);

        Assert.assertTrue(result.startsWith("This is a sample file content!"));
        Assert.assertTrue(result.contains("'Christoph'"));
    }

    @Test(expectedExceptions = InvalidFunctionUsageException.class)
    public void testInvalidFunctionUsage() {
        function.execute(Collections.<String>emptyList(), context);
    }

}