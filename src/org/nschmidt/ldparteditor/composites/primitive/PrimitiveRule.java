/* MIT - License

Copyright (c) 2012 - this year, Nils Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package org.nschmidt.ldparteditor.composites.primitive;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.nschmidt.ldparteditor.data.Primitive;
import org.nschmidt.ldparteditor.enums.Rule;

public class PrimitiveRule {


    private final Pattern pattern;
    private boolean not = false;
    private boolean and = false;
    private boolean function = false;
    private String criteria = ""; //$NON-NLS-1$
    private Rule rule;
    public PrimitiveRule(Rule rule) {
        this.rule = rule;
        this.function = true;
        this.pattern = Pattern.compile("[^.*]"); //$NON-NLS-1$
        this.and = true;
    }

    public PrimitiveRule(Rule rule, String criteria, boolean hasAnd, boolean hasNot) {
        this.rule = rule;
        this.criteria = criteria;
        this.not = hasNot;
        this.and = hasAnd;
        this.function = false;
        Pattern pattern2;
        try {
            pattern2 = Pattern.compile(criteria);
        } catch (PatternSyntaxException ex) {
            pattern2 = Pattern.compile("[^.*]"); //$NON-NLS-1$
        }
        pattern = pattern2;
    }


    public boolean matches(Primitive p) {
        switch (rule) {
        case CONTAINS:
            return p.getDescription().contains(criteria);
        case ENDS_WITH:
            return p.getDescription().endsWith(criteria);
        case FILENAME_CONTAINS:
            return p.getName().contains(criteria);
        case FILENAME_ENDS_WITH:
            return p.getName().endsWith(criteria);
        case FILENAME_MATCHES:
            return pattern.matcher(p.getName()).matches();
        case FILENAME_ORDER_BY_FRACTION:
        case FILENAME_ORDER_BY_LASTNUMBER:
            return true;
        case FILENAME_STARTS_WITH:
            return p.getName().startsWith(criteria);
        case MATCHES:
            return pattern.matcher(p.getDescription()).matches();
        case STARTS_WITH:
            return p.getDescription().startsWith(criteria);
        default:
            break;
        }
        return false;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public boolean isAnd() {
        return and;
    }

    public void setAnd(boolean and) {
        this.and = and;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public String toString() {
        return (isAnd() ? "AND " : "OR ") + (isNot() ? "NOT " : "") + rule.toString() + (criteria.isEmpty() ? "" : " '" + criteria + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    }

    public boolean isFunction() {
        return function;
    }

    public void setFunction(boolean function) {
        this.function = function;
    }
}
