package com.test;

import com.test.Another;
import com.test.Test;

/**
 * Generated by JAnnocessor
 */
@Test(Another.class)
public class TestProcess {

    private Another getView() {
        return null;
    }

    protected void invokeSetPropertyMethod(String property, Boolean value) {
        if(property.equals("param_1")){ getView().setProp(value);};
        if(property.equals("param_2")){ getView().setProp_2(value);};

    }

    protected String invokeGetPropertyMethod(String property) {
        if(property.equals("param_1")){return (String) getView().getProp();};
        if(property.equals("param_2")){return (String) getView().getProp_2();};
        return null;
    }

}