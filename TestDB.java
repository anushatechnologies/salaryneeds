import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/salaryneeds?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String[] passwords = {"", "root", "Aparna124@"};

        for (String pwd : passwords) {
            System.out.println("Trying password: '" + pwd + "'");
            try {
                Connection conn = DriverManager.getConnection(url, user, pwd);
                System.out.println("SUCCESS with password: '" + pwd + "'");
                conn.close();
                return;
            } catch (Exception e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }
}
