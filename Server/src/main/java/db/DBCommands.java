package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBCommands {

    public boolean auth(String login, String password) {
        PreparedStatement ps;
        try {
            ps = DataSource.getConnection().prepareStatement("select * from auth where login=? and password=?");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet set = ps.executeQuery();
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loginExists(String login) {
        PreparedStatement ps;
        try {
            ps = DataSource.getConnection().prepareStatement("select * from auth where login=?");
            ps.setString(1, login);
            ResultSet set = ps.executeQuery();
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void registration(String login, String password) {
        PreparedStatement ps;
        try {
            ps = DataSource.getConnection().prepareStatement("insert into auth (login, password) values (? , ?)");
            ps.setString(1, login);
            ps.setString(2, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
