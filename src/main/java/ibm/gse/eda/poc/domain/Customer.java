package ibm.gse.eda.poc.domain;

public class Customer {

    protected String lastname;
    protected String firstname;
    protected String userid;
    protected String dob;

    public Customer(){}

	public Customer(String userid,String name, String fname,  String dob) {
        this.userid = userid;
        this.lastname = name;
        this.firstname = fname;
        this.dob = dob;
	}
}