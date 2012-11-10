package com.test.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jannocessor.collection.api.PowerList;
import org.jannocessor.extra.processor.AbstractGenerator;
import org.jannocessor.model.executable.JavaMethod;
import org.jannocessor.model.modifier.MethodModifiers;
import org.jannocessor.model.modifier.value.MethodModifierValue;
import org.jannocessor.model.structure.AbstractJavaClass;
import org.jannocessor.model.util.New;
import org.jannocessor.model.variable.JavaParameter;
import org.jannocessor.processor.api.ProcessingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.Property;
import com.test.Test;

public class MyProcessor extends AbstractGenerator<AbstractJavaClass> {
	private final Logger log = LoggerFactory.getLogger(MyProcessor.class);
	private static final String PROPERTY_PARAM = "property";
	private static final String VALUE_PARAM = "value";
	
	public MyProcessor(String beanPkg, String destPackage, boolean inDebugMode) {
		super(destPackage, inDebugMode);

	}

	@SuppressWarnings("unused")
	@Override
	protected void generateCodeFrom(PowerList<AbstractJavaClass> models,
			ProcessingContext context) {
		MethodModifiers modifiers = New.methodModifiers(MethodModifierValue.PROTECTED);
		for (AbstractJavaClass model : models) {
			JavaParameter getterParameter = New.parameter(String.class, PROPERTY_PARAM);
			JavaMethod getterMethod = New.method(modifiers,New.type("String"), "invokeGetPropertyMethod", getterParameter);

			//TODO create as much invokeSetProperty as input types while looping trough the methods
			JavaParameter setterParameter = New.parameter(Boolean.class, VALUE_PARAM);
			JavaMethod setterMethod = New.method(modifiers,New.type("void"), "invokeSetPropertyMethod", getterParameter, setterParameter);
			
			//1 --- CLEAN UP existing methods
			for(JavaMethod method: model.getMethods()){
				if(method.getName().equals(getterMethod.getName())){
					if(method.getParameters().size() == getterMethod.getParameters().size())//FIXME better compare
					{
							model.getMethods().remove(method);
							break;
					}
				}	
			}
			for(JavaMethod method: model.getMethods()){
				if(method.getName().equals(setterMethod.getName())){
					if(method.getParameters().size() == setterMethod.getParameters().size())//FIXME better compare
					{
						model.getMethods().remove(method);
						break;
					}
				}
			}
			
			model.getMethods().add(setterMethod);
			model.getMethods().add(getterMethod);

			/**
			 * set method body
			 */
			Map<String,String> inMap = new HashMap<String, String>();
			Map<String,String> outMap = new HashMap<String, String>();
			try {
				Annotation[] list = getClass().getClassLoader().loadClass(model.getType().getCanonicalName()).getAnnotations();
				if(list[0] instanceof Test){
					Test myAnnotation = (Test) list[0];
					Class<?> propertyClass = myAnnotation.value();
					
					for(Method m: propertyClass.getMethods()){
						if(m.getAnnotations().length ==1 && m.getAnnotations()[0] instanceof Property){
							Property p = (Property) m.getAnnotations()[0];
							
							if("void".equals(m.getReturnType().toString())){//getter
								outMap.put(p.value(),m.getName());
							}else{//setter
								inMap.put(p.value(),m.getName());
							}
						}
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			getterMethod.getBody().setHardcoded(getHardCodedGetterBody(inMap));
			setterMethod.getBody().setHardcoded(getHardCodedSetterBody(outMap));
			context.generateCode(model,null, false);
		}

	}
	
	/*
	 * will return
	 * if(property.equals(key){return (String) getView().value()}
	 */
	private String getHardCodedGetterBody(Map<String,String> map){
		StringBuilder b = new StringBuilder();
		for(String key: map.keySet()){
			String value = map.get(key);
			b.append(getGetterMask( key,  value)).append("\n");
		}
		return b.append("return null;").toString();
	}
	
	private String getGetterMask(String key, String value){
		return String.format("if("+PROPERTY_PARAM+".equals(\"%s\")){return (String) getView().%s();};", key,value);
	}
	
	private String getSetterMask(String key, String value){
		return String.format("if("+PROPERTY_PARAM+".equals(\"%s\")){ getView().%s("+VALUE_PARAM+");};", key,value);
	}
	
	private String getHardCodedSetterBody(Map<String,String> map){
		StringBuilder b = new StringBuilder();
		for(String key: map.keySet()){
			String value = map.get(key);
			b.append(getSetterMask( key,  value)).append("\n");
		}
		return b.toString();
	}

}
