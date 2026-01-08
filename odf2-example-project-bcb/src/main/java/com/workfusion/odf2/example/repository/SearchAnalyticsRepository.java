package com.workfusion.odf2.example.repository;

import java.sql.SQLException;

import com.j256.ormlite.support.ConnectionSource;

import com.workfusion.odf2.core.orm.OrmLiteRepository;
import com.workfusion.odf2.example.model.SearchesAnalytics;

public class SearchAnalyticsRepository extends OrmLiteRepository<SearchesAnalytics> {

    public SearchAnalyticsRepository(ConnectionSource connectionSource, Class<SearchesAnalytics> entityType) throws SQLException {
        super(connectionSource, entityType);
    }

}
