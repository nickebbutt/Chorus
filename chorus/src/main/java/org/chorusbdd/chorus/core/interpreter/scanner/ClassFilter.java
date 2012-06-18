/**
 *  Copyright (C) 2000-2012 The Software Conservancy as Trustee.
 *  All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to
 *  deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 *  sell copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 *
 *  Nothing in this notice shall be deemed to grant any rights to trademarks,
 *  copyrights, patents, trade secrets or any other intellectual property of the
 *  licensor or any contributor except as expressly stated herein. No patent
 *  license is granted separate from the Software, for code that you delete from
 *  the Software, or for combinations of the Software with other software or
 *  hardware.
 */
package org.chorusbdd.chorus.core.interpreter.scanner;

/**
 * Created with IntelliJ IDEA.
 * User: nick
 * Date: 11/05/12
 * Time: 15:31
 *
 * Class filter for classpath class scanning
 *
 *
 */
public interface ClassFilter {

    public static final ClassFilter NULL_PERMISSIVE_FILTER = new ClassFilter() {
        public boolean acceptByName(String className) {
            return false;
        }

        public boolean acceptByClass(Class clazz) {
            return false;
        }
    };

    public static final ClassFilter NULL_RESTRICTIVE_FILTER = new ClassFilter() {
        public boolean acceptByName(String className) {
            return false;
        }

        public boolean acceptByClass(Class clazz) {
            return false;
        }
    };

    /**
     *  A first stage in filtering - check the class passes filter by it's class/package name
     *
     * @return true, if the Class with the given fully qualified class name passes the filter
     */
    public boolean acceptByName(String className);

    /**
     * A second stage of filtering, only performed if stage one has passed filter checks
     * Check that the instantiated Class instance passes filters (e.g. using annotations)
     *
     * This requires class loading to take place, it is best to use the filter by class
     * name where possible
     *
     * @return true, if the Class clazz passes the filter criteria
     */
    public boolean acceptByClass(Class clazz);
}
