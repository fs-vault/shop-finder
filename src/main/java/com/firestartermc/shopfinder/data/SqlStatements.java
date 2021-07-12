package com.firestartermc.shopfinder.data;

public class SqlStatements {

    public static final String SELECT_ALL = "SELECT * FROM realmdata.shop_signs;";
    public static final String RECORD_SIGN = "INSERT INTO realmdata.shop_signs (item, type, seller, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?, ?);";
    public static final String DELETE_SIGN = "DELETE FROM realmdata.shop_signs WHERE world = ? AND x = ? AND y = ? and z = ?;";
}
