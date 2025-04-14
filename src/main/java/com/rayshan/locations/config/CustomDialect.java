//package com.rayshan.locations.config;
//
//import org.hibernate.community.dialect.SQLiteDialect;
//import org.hibernate.dialect.Dialect;
//import org.hibernate.dialect.identity.IdentityColumnSupport;
//import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
//import org.hibernate.dialect.pagination.LimitHandler;
//import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;
//
//import java.sql.Types;
//import java.util.HashMap;
//import java.util.Map;
//
//public class CustomDialect extends SQLiteDialect {
//
//    public CustomDialect() {
//        super();
//    }
//
//    @Override
//    protected Map<Integer, String> initializeColumnTypeMappings() {
//        Map<Integer, String> mappings = new HashMap<>();
//        mappings.put(Types.INTEGER, "integer");
//        mappings.put(Types.VARCHAR, "varchar");
//        mappings.put(Types.BOOLEAN, "boolean");
//        mappings.put(Types.FLOAT, "float");
//        mappings.put(Types.DOUBLE, "double");
//        mappings.put(Types.DATE, "date");
//        mappings.put(Types.TIMESTAMP, "datetime");
//        mappings.put(Types.BLOB, "blob");
//        mappings.put(Types.CLOB, "text");
//        return mappings;
//    }
//
//    @Override
//    public IdentityColumnSupport getIdentityColumnSupport() {
//        return new IdentityColumnSupportImpl();
//    }
//
//    @Override
//    public LimitHandler getLimitHandler() {
//        return LimitOffsetLimitHandler.INSTANCE;
//    }
//
//    @Override
//    public boolean supportsIfExistsBeforeTableName() {
//        return true;
//    }
//
//    @Override
//    public boolean supportsIfExistsAfterTableName() {
//        return false;
//    }
//
//    @Override
//    public boolean supportsTemporaryTables() {
//        return true;
//    }
//
//    @Override
//    public boolean supportsCascadeDelete() {
//        return false;
//    }
//
//    @Override
//    public String getCurrentTimestampSelectString() {
//        return "select current_timestamp";
//    }
//}
//
