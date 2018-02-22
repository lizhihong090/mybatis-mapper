package com.own.generator;

import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;

public class MyShellCallback extends DefaultShellCallback {

	public MyShellCallback(boolean overwrite) {
		super(overwrite);
	}
	
	/**
	 * 判断是否绝对路径
	 * @param path
	 * @return
	 */
	private boolean isRelativePath(String path) {
		if (path.startsWith(File.separator) || path.contains(":")) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public File getDirectory(String targetProject, String targetPackage) throws ShellException {
		// TODO Auto-generated method stub
		if (isRelativePath(targetProject)) {
			String basePath = System.getProperty("user.dir");
			targetProject = basePath + File.separatorChar + targetProject;
		}
		
		
		return super.getDirectory(targetProject, targetPackage);
		
	}
}
