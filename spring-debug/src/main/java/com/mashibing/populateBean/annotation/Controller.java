package com.mashibing.populateBean.annotation;


import org.springframework.beans.factory.annotation.Lookup;

import javax.annotation.Resource;


public abstract class Controller {
    @Resource
    protected StudentService service;

    @Lookup
    public abstract LookupTestPrototype lookupTestPrototype ();
}
