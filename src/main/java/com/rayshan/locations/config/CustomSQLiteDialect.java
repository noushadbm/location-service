//package com.rayshan.locations.config;
//
//import org.hibernate.community.dialect.SQLiteDialect;
//import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;
//
//public class CustomSQLiteDialect extends SQLiteDialect {
//    @Override
//    public void initializeDdlTypeRegistry(DdlTypeRegistry ddlTypeRegistry) {
//        super.initializeDdlTypeRegistry(ddlTypeRegistry);
//
//        // Override the BIGINT mapping to use INTEGER in SQLite
//        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(SqlTypes.BIGINT, "integer", this));
//    }
//}
