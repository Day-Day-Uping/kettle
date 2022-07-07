package com.kettle.mykettle.service;

import com.kettle.mykettle.dto.DataSourceInfo;

import java.util.List;

public interface DataSourceService {
    List<DataSourceInfo> getDataSource();
    List<String> getKtrName();
}
