package org.jannocessor.config;

import org.jannocessor.model.structure.JavaClass;
import org.jannocessor.processor.annotation.Annotated;
import org.jannocessor.processor.annotation.Types;

import com.test.Test;
import com.test.processor.MyProcessor;

public class Processors {

	@Annotated(Test.class)
	@Types(JavaClass.class)
	public MyProcessor willProcessMyAnnotatedClasses() {
		return new MyProcessor("com.test", "com.test", false);
	}

}