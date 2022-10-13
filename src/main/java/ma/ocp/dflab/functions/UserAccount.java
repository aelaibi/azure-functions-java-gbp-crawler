package ma.ocp.dflab.functions;

public class UserAccount {

    private String account;
    private String pwd;


    public UserAccount account(String acc){
        this.setAccount(acc);
        return this;
    }

    public UserAccount password(String pass){
        this.setPwd(pass);
        return this;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
