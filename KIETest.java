package com.agp.cloud.ruleset.executor;

import lombok.extern.slf4j.Slf4j;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author: create by agp
 * @description: ruleset-executor
 * 
 */
@Slf4j
@RunWith(JUnit4.class)
public class KIETest {
    @Test
    public void test() throws Exception{
        String str="package org.abcs \n";
        str+="import java.math.BigDecimal;\n";
        str+="global java.util.List list \n";
        str+="rule rule1\n";
        str+="    dialect \"java\" \n";
        str+="when \n";
        str+="    $bd : BigDecimal() \n";
        str+="    eval( $bd.compareTo( BigDecimal.ZERO ) >0 )  \n";
        str+="then \n";
        str+="    list.add( $bd ); \n";
        str+="end  \n";
        KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        knowledgeBuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), ResourceType.DRL);
        if (knowledgeBuilder.hasErrors()){
            log.error(knowledgeBuilder.getErrors().toString());
        }
        assertFalse(knowledgeBuilder.hasErrors());
        KnowledgeBase knowledgeBase = knowledgeBuilder.newKnowledgeBase();
        StatefulKnowledgeSession kieSession = knowledgeBase.newStatefulKnowledgeSession();
        List list=new ArrayList();
        kieSession.setGlobal("list",list);
        kieSession.insert(new BigDecimal(1.5));
        kieSession.fireAllRules();
        assertEquals(1,list.size());
    }

}
