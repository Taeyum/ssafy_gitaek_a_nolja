package com.ssafy.exam.listener;

import com.ssafy.exam.model.dao.MemberDao;
import com.ssafy.exam.model.dao.TripPlanDaoImpl;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class MyContextListener implements ServletContextListener {
	// 파일로드 하는 작업은 listener 에서 해야함

	MemberDao memberDao = MemberDao.getInstance();

	// Q1-1. 프로젝트 실행시 아래 코드를 확인하여 작성하세요
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("contextInitialized() called");
		TripPlanDaoImpl.getInstance().ensureTables();
		memberDao.tableCheck();
	}

	// Q1-2. 프로젝트 실행시 아래 코드를 확인하여 작성하세요
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("contextDestroyed() called");
		memberDao.save();

	}

}
