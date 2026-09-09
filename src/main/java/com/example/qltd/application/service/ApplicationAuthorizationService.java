package com.example.qltd.application.service;

import com.example.qltd.application.entity.Application;
import com.example.qltd.user.entity.User;

public interface ApplicationAuthorizationService {

    void checkCanManage(
            Application application,
            User user);

    void checkCanManageJob(
            Long jobId,
            User user);
}