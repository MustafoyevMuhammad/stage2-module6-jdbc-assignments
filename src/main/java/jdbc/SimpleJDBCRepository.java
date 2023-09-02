package jdbc;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {
    private final CustomDataSource dataSource = CustomDataSource.getInstance();
    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private static final String createUserSQL = "INSERT INTO myusers (firstname, lastname, age) VALUES (?, ?, ?)";
    private static final String updateUserSQL = "UPDATE myusers SET firstname=?, lastname=?, age=? WHERE id=?";
    private static final String deleteUserSQL = "DELETE FROM myusers WHERE id=?";
    private static final String findUserByIdSQL = "SELECT * FROM myusers WHERE id=?";
    private static final String findUserByNameSQL = "SELECT * FROM myusers WHERE firstname=?";
    private static final String findAllUserSQL = "SELECT * FROM myusers";

    public void getConnection() {

    }

    //    public SimpleJDBCRepository(User user) {
//        // Initialize the connection in the constructor
//        try {
//            connection = CustomConnector.getConnection();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public Long createUser(User user) {
        try (var connection = dataSource.getConnection();
             var ps = connection.prepareStatement(createUserSQL, Statement.RETURN_GENERATED_KEYS);
        ){
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setInt(3, user.getAge());
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    public User findUserById(Long userId) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(findUserByIdSQL)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getInt("age")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User findUserByName(String userName) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(findUserByNameSQL)) {

            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getInt("age")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<User> findAllUser() {
        List<User> userList = new ArrayList<>();
        try (var conn = dataSource.getConnection()) {
            st = connection.createStatement();
            ResultSet rs = st.executeQuery(findAllUserSQL);

            while (rs.next()) {
                userList.add(new User(
                        rs.getLong("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getInt("age")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userList;
    }

    public boolean updateUser(User user) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(updateUserSQL)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setInt(3, user.getAge());
            ps.setLong(4, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteUser(Long userId) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(deleteUserSQL)) {

            ps.setLong(1, userId);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}