package com.workfusion.odf2.example.repository;

import com.workfusion.odf2.example.model.Attachment;

import java.sql.SQLException;

import com.j256.ormlite.support.ConnectionSource;

import com.workfusion.odf2.core.orm.OrmLiteRepository;

public class AttachmentRepository extends OrmLiteRepository<Attachment> {

    public AttachmentRepository(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Attachment.class);
    }

}
