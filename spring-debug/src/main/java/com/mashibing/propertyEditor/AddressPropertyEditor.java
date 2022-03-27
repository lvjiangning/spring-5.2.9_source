package com.mashibing.propertyEditor;

import java.beans.PropertyEditorSupport;

import static com.oracle.jrockit.jfr.ContentType.Address;

/**
 * 属性编辑器
 */
public class AddressPropertyEditor  extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] s = text.split("_");
        Azddress address = new Address();
        address.setProvince(s[0]);
        address.setCity(s[1]);
        address.setTown(s[2]);
        this.setValue(address);
    }
}
