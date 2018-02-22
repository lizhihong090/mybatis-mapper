package com.own.generator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

/**
 * 自动生成bean，dao
 *
 */
public class MybatisBeanDaoGenerator {
	public static void generator() throws Exception {
		try{
			List<String> warnings = new ArrayList<String>();
			boolean overwrite = true;
			ConfigurationParser cp = new ConfigurationParser(warnings);
			URL url = ClassLoader.getSystemResource("mybatis.xml");
			File configFile = new File(url.getPath());
			Configuration config = cp.parseConfiguration(configFile);
			DefaultShellCallback callback = new MyShellCallback(overwrite);
			MyBatisGeneratorEx myBatisGenerator = new MyBatisGeneratorEx(config, callback, warnings);
			myBatisGenerator.generate(null);
			for (String warning : warnings) {
				System.out.println(warning);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
}
