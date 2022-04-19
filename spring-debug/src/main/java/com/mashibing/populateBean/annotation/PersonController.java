package com.mashibing.populateBean.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Controller
@DependsOn("dependController")
public class PersonController extends com.mashibing.populateBean.annotation.Controller {
    @Autowired
    private PersonService personService;

    @Autowired
    @Lazy
    private TeacherController teacherController;

    @Override
    public LookupTestPrototype lookupTestPrototype() {
        return null;
    }

}
