package io.plactal.eoscommander.ui.account.listview;

/**
 * Created by yangtao on 2018/11/7
 */
public class AccountBalance {

    private String name;

    private String balance;

    public AccountBalance(String name, String balance) {
        this.name = name;
        this.balance = balance;
    }

    public AccountBalance(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBalance() {
        return balance;

    }

    public void setBalance(String balance) {
        this.balance = balance;
    }


}
