package app_interface;

//Import necessary extensions
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
	
	
public class Online_Grocery_Store{

//METHODS	
		public static Connection connect() {
			final String url = "jdbc:postgresql://localhost/Online Grocery Store";
	    	final String user = "postgres";
	    	final String password = "machZ0125_";
	    	
	    	try {
	    		Connection conn = DriverManager.getConnection(url, user, password);
	    		return conn;
	    	}
	    	catch (SQLException e) {
	    		System.out.println(e.getMessage());
	    		return null;
	    	}
		}
		
		public static Statement initializeSQL(Connection c) {
			Statement s=null;
			try {
				s = c.createStatement();
			} 
			catch (SQLException e) {
				System.out.print(e.getMessage());
			}
			return s;
		}
		
		public static void displayChoices(String... options) {
			int i = 1;
			System.out.println();
			for(String option: options) {
				System.out.println("\t["+i+"]\t"+option);
				i++;
			}
		}
		
		public static void displayChoices(ResultSet rs) {
			int i = 1;
			try {	
				int num_columns = rs.getMetaData().getColumnCount();
				while(rs.next()) {
					System.out.print("\n\t["+i+"]\t");
					for (int n=1; n<=num_columns; n++) {
						System.out.printf("%-15s",rs.getString(n));
					}
					i++;
				}
	    	}
	    	catch (SQLException e) {
	    		System.out.println(e.getMessage());
	    	}	
		}

		
//MAIN CLASS		
	    public static void main(String[] args) {
	    	
	        //declare variables
	    	Scanner in = new Scanner(System.in);
	    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");  
	    	int op_choice = 0; //keeps track of which option user chooses
	    	int user_id = 0;
	    	String user_pass = "";
	    	int order_id = 1;
	    	int incart_id = 1;
	    	int product_id = 0;
	    	double order_total = 0;
	    	ResultSet user_info;
	        
	    	//prep sql
	    	Connection conn = connect();
	    	Statement s1 = initializeSQL(conn); //this one designated to hold user_info
	    	Statement s2 = initializeSQL(conn);
	    	ResultSet rs = null;
	    	
	    	
	    	try {
	    
	    		//welcome and login
	    		System.out.print("\n\nWelcome to this server for online grocery shopping!\n\n");
	    		boolean login = true;
	    		while (login) {
	    			System.out.print("\nPlease enter your Customer or Employee ID: ");
	    			user_id = in.nextInt();
		    		System.out.print("Please enter your password: ");
		    		user_pass = in.next();
		    		
		    		//test if customer
		    		if ((s2.executeQuery("select * "+
							"from customer "+
							"where customer_id='"+user_id+
							"' and password='"+user_pass+"'")).next()) {
		    			
		    			user_info = s1.executeQuery("select * from customer natural join address "+ 
								   					"where customer_id = '"+user_id+"'");
		    			login = false;
		    			
		    			
		    			
		    			//CUSTOMER VIEW
		    			
			    		//home menu
				    	user_info.next();
			    		System.out.println("\n\n\n\nWelcome, "+user_info.getString("name")+"!\n\n");
			    		System.out.println("Please choose an option below:");
			    		displayChoices("Start shopping", 
			    					   "Continue previous session",
			    					   "Edit profile information",
		            				   "Exit");		    	
				    	op_choice = in.nextInt();
				    	
				    	
				    	//shopping...
				    	if (op_choice==1 || op_choice==2) {
				    		
				    		//previous session - load the existing order
				    		if (op_choice==2) {
				    			rs = s2.executeQuery("select distinct order_id "+
				    								"from orders "+
				    								"where customer_id = "+user_id+
				    								" and status = 'unfinished';");
				    			if (rs.next()) {
				    				System.out.println("Which order would you like to continue? (Order ID is on the right)");
				    				rs = s2.executeQuery("select distinct order_id "+
		    											 "from orders "+
		    											 "where customer_id = "+user_id+
		    											 " and status = 'unfinished';");
				    				displayChoices(rs);
					    			rs = s2.executeQuery("select distinct order_id "+
		    											 "from orders "+
		    											 "where customer_id = "+user_id+
		    											 " and status = 'unfinished';");
					    			op_choice = in.nextInt();
					    			for (int i=0; i<op_choice; i++) {
					    				rs.next();
					    			}
					    			order_id = rs.getInt("order_id");
					    			rs = s2.executeQuery("select max(incart_id) "+
					    								 "from cart_items "+
					    								 "where order_id = "+order_id);
					    			rs.next();
					    			incart_id = rs.getInt(1)+1;
				    			}
				    			else {
				    				System.out.println("No previous orders found for this user.");
				    				break;
				    			}
				    		}
				    		
				    		//new session - generate the next order_id number
				    		else if (op_choice==1) {
				    			rs = s2.executeQuery("select max(order_id)"+
										   			"from orders");
				    			if (rs.next()) {
				    				int max_order_id = rs.getInt(1);
					    			order_id = max_order_id + 1;
				    			}
				    			else {
				    				order_id = 1;
				    			}
				    			s2.executeUpdate("insert into orders values('"+
				    							 order_id+"','"+
				    							 user_id+"','"+
				    							 "unfinished', null, null)");
				    		}
				    		
				    		boolean shopping = true;
				    		while (shopping) {
				    			
				    			//display inventory and prompt user
					    		rs = s2.executeQuery("select product_name, price from product order by product_id;");
					    		System.out.println("Select the item you would like to add to cart: ");
								displayChoices(rs);
					    		op_choice = in.nextInt();
					    		rs = s2.executeQuery("select product_id from product order by product_id;");
					    		for (int i=0; i<op_choice; i++) {
				    				rs.next();
				    			}
				    			product_id = rs.getInt("product_id");
					    		System.out.println("\nHow many would you like to add? (<100) ");
								int qty = in.nextInt();
								
								//add specified quantity of items to cart
								s2.executeUpdate("insert into cart_items values('"+
												order_id+"','"+
												incart_id+"','"+
												product_id+"','"+
												qty+"')");
								System.out.println(qty+" item(s) added to cart.");
								
								//continue, pause, or check out
								System.out.println("\n\n\nWhat would you like to do next?");
								displayChoices("Continue shopping",
											   "Check out",
											   "Save and exit");
								op_choice = in.nextInt();
								
								//checkout
								if (op_choice == 2) {
									rs = s2.executeQuery("select product_name, sum(quantity) as quantity "+
														"from cart_items natural join product "+
														"where order_id = "+order_id+
														" group by product_name");
									System.out.printf("\n\n  %-15s%-15s%n", "Item", "Quantity");
									System.out.println("==================================");
									while(rs.next()) {
											System.out.printf("  %-19s%-15s%n",rs.getString("product_name"),rs.getString("quantity"));
									}
									rs = s2.executeQuery("select sum(price*quantity) " +
														 		  "from cart_items natural join product " + 
														 		  "where order_id = "+order_id);
									rs.next();
									order_total = rs.getDouble(1);
									System.out.printf("%nTotal: $%-10.2f%n",order_total);
									System.out.print("Press 1 to confirm order\n\n");
									op_choice = in.nextInt();
									
									
									//confirm order
									rs = s2.executeQuery("select balance from customer where customer_id = "+user_id);
									rs.next();
									s2.executeUpdate("update customer set balance = "+(rs.getDouble(1)+order_total)+
																					" where customer_id = "+user_id);
									rs = s2.executeQuery("select max(tracking_number) from orders");
									rs.next();
									s2.executeUpdate("update orders set status = 'issued', "+
																		"time_stamp = '"+dtf.format(LocalDateTime.now())+"',"+
																		"tracking_number = "+(rs.getInt(1)+1)+
																		"where order_id = "+order_id);
									//print receipt
									System.out.println("\tRECEIPT\n==================================");
									System.out.println("Order confirmed on "+dtf.format(LocalDateTime.now()));
									System.out.println("Paid with credit card "+user_info.getString("card_number"));
									System.out.println("Will be shipped to "+user_info.getString("street_address")+" "+
																			 user_info.getString("city")+", "+
																			 user_info.getString("state")+" "+
																			 user_info.getString("zip"));
									shopping = false;
								}
								
								//save cart for later
								if (op_choice == 3) {
									System.out.println("Cart saved! Order ID is "+ order_id);
									shopping = false;
								}
								incart_id++;
				    		}
				    		op_choice = 0;
				    	}
				    	
				    	//editing profile information...
				    	else if (op_choice==3) {
				    		
				    		//display current information
				    		System.out.println("\n\nCurrent profile information:\n");
				    		System.out.printf("  %-30s%s%n", "Name:", user_info.getString("name"));
				    		System.out.printf("  %-30s%s%n", "Credit card number:", user_info.getString("card_number"));
				    		System.out.printf("  %-30s%s %s, %s %s%n", "Address:", user_info.getString("street_address"),
				    															   user_info.getString("city"),
				    															   user_info.getString("state"),
				    															   user_info.getString("zip"));
				    		
				    		boolean editing = true;
				    		while (editing) {
				    			System.out.println("\n\nWhat information would you like to edit?");
					    		displayChoices("Name",
					    					   "Password",
					    					   "Credit card number",
					    					   "Address",
					    					   "Done");
					    		op_choice = in.nextInt();
					    		
					    		//editing name...
					    		if (op_choice == 1) {
					    			System.out.println("Please type your updated name: ");
					    			String name = in.next();
					    			s2.executeUpdate("update customer set name = '"+name+"' where customer_id = "+user_id);
					    		}
					    		
					    		//editing password...
					    		else if (op_choice == 2) {
					    			System.out.println("Please type your current password: ");
					    			if (in.next().equals(user_info.getString("password"))) {
					    				System.out.println("Please type your new password: ");
					    				String password = in.next();
						    			s2.executeUpdate("update customer set password = '"+password+"' where customer_id = "+user_id);
					    			}
					    			else {
					    				System.out.println("Incorrect password.\n");
					    			}
					    		}
					    		
					    		//editing card number...
					    		else if (op_choice == 3) {
					    			System.out.println("Please type your updated credit card number: ");
					    			String card_number = in.next();
					    			s2.executeUpdate("update customer set card_number = '"+card_number+"' where customer_id = "+user_id);
					    		}
					    		
					    		//editing address...
					    		else if (op_choice == 4) {
					    			in.nextLine();
					    			System.out.println("Please enter your updated street address: ");
					    			String st_address = in.nextLine();
					    			System.out.println("Please enter your updated city: ");
					    			String city = in.nextLine();
					    			System.out.println("Please enter your updated state (XX): ");
					    			String state = in.nextLine();
					    			System.out.println("Please enter your updated zip code: ");
					    			String zip = in.nextLine();
					    			s2.executeUpdate("update address set street_address = '"+st_address+"', "+
					    												  "city = '"+city+"', "+
					    												  "state = '"+state+"', "+
					    												  "zip = '"+zip+
					    												  "' where address_id = "+user_info.getString("address_id"));
					    			System.out.println("\nUpdated successfully!");
					    		}
					    		
					    		else {
					    			editing = false;
					    		}
				    		}
				    	}	
		    		}
		    		
		    		else if ((s2.executeQuery("select * "+
											 "from employee "+
											 "where employee_id='"+user_id+
											 "' and password='"+user_pass+"'")).next()) {

		    			user_info = s2.executeQuery("select * from employee natural join address "+ 
								   "where employee_id = '"+user_id+"'");
		    			login = false;
		    			
		    			
		    			
		    			//EMPLOYEE VIEW HERE
		    			
		    			user_info.next();
			    		System.out.println("\n\n\n\nWelcome, "+user_info.getString("name")+"!\n\n");
			    		System.out.println("Please choose an option below:");
				    		displayChoices("Add a product and product pricing.", 
				    					   "Delete a product and product pricing.",
				    					   "Edit a product and product pricing.",
			            				   "Add stock to a warehouse.",
			            				   "Exit");		    	
				    		op_choice = in.nextInt();
		    			
				    		if (op_choice==1) {
				    			String productName ="";
				    			int productid=0;
				    			float price = 0;
				    			String dept = "";
				    			System.out.println("Which product would you like to add?");
				    			productName = in.next();
				    			System.out.println("What is the unit price of "+ productName+"?");
				    			price = in.nextFloat();
				    			System.out.println("In which department should this product be found?");
				    			dept = in.next();
				    			rs= s2.executeQuery("select max(product_id)"+
			    											"from product");
				    			if (rs.next()) {
				    				productid= rs.getInt(1);
				    				productid++;
				    			}
				    			else {
				    				productid=1;
				    			}
				    			s2.executeUpdate("insert into product values"
				    					+ "("+productid+","+price+",'"+productName+"','"+dept+"')");
				    			System.out.println("Product added!\n");
				    		}
				    		//delete item from stock
				    		else if (op_choice==2) {
				    			rs = s1.executeQuery("select product_id, product_name "+
				    								"from product;");
				    			System.out.println("Which product would you like to delete?");
				    			displayChoices(rs);
				    			product_id = in.nextInt();
				    			System.out.println("delete from product "+
		    							"where product_id = "+product_id);
				    			s2.executeUpdate("delete from product "+
				    							"where product_id = "+product_id);
				    			System.out.println("Product deleted!\n");
				    		}
				    		else if (op_choice==3) {
				    			float price =0;
				    			int productid=0;
				    			rs = s2.executeQuery("select product_id, product_name "+
										"from product;");
				    			System.out.println("Which product would you like to edit?");
				    			displayChoices(rs);
				    			productid = in.nextInt();
				    			System.out.println("Price of product:");
				    			price = in.nextFloat();
				    			System.out.println();
				    			s2.executeUpdate("update product "+
				    								"set price = "+price+
				    								" where product_id="+productid);
				    			System.out.println("Product edited!\n");
				    		}
				    		else if (op_choice==4) {
					    		int warehouseID = 0;
					    		int productID = 0;
					    		int stock = 0;
					    		int capacity = 0;
					    		int initialStock = 0;
					    		int initialProdStock = 0;
					    		int finalProdStoc = 0;
					    		int newStock = 0;
					    		rs = s2.executeQuery("select warehouse.warehouse_id, address.street_address, address.city,"
					    				+ "address.state, address.zip"+
										" from warehouse, address "
										+ "where warehouse.address_id=address.address_id;");
					    		displayChoices(rs);
					    		System.out.println("\nIn Which warehouse ID?");
					    		warehouseID = in.nextInt();
					    		rs = s2.executeQuery("select product_id, product_name "+
										"from product;");
					    		displayChoices(rs);
				    			System.out.println("\nWhich product would you like to add to the stock?");
				    			productID = in.nextInt();
				    			rs = s2.executeQuery("select stock"
					    				+ " from stores"
										+ " where product_id="+productID
										+ " and warehouse_id="+warehouseID);
				    			if (rs.next()){
				    				initialProdStock = rs.getInt(1);
				    			}
				    			System.out.println("\nQuantity of product added to stock:");
				    			stock = in.nextInt();
				    			finalProdStoc = stock+initialProdStock;
				    			rs = s2.executeQuery("select sum(stores.stock)"
					    				+" from stores"
										+ " where warehouse_id="+warehouseID+";");
				    			if (rs.next()){
				    				initialStock = rs.getInt(1);
				    			}
				    			newStock = initialStock + stock;
				    			rs = s2.executeQuery("select warehouse.capacity"
				    					+" from warehouse"
				    					+" where warehouse.warehouse_id="+warehouseID+";");
				    			if (rs.next()){
				    				capacity = rs.getInt(1);
				    			}
				    			System.out.println("\ncapacity: "+capacity+"\nnewStock:"+newStock);
				    			// Store it as capacity
				    			if (capacity<newStock) {
				    				System.out.println("The stock has been exceeded, the product will not be stored!");
				    			}
				    			else {
				    				if (initialProdStock>0) {
					    				s2.executeUpdate("update stores"
											+" set stock = "+finalProdStoc
											+" where warehouse_id="+ warehouseID
											+" and product_id = "+ productID);
					    				System.out.println("The stock has been updated!");
				    				}
				    				else {
				    					System.out.println("insert into stores values("+
				    							 warehouseID+","+
				    							 productID+","+
				    							 finalProdStoc+")");
				    					s2.executeUpdate("insert into stores values("+
				    							 warehouseID+","+
				    							 productID+","+
				    							 finalProdStoc+")");
				    				}
				    			}
					    	}
				    		else if (op_choice ==5) {
				    		}
				    		else {
				    			System.out.println("Invalid command!");
				    		}
		    		}
		    		
		    		else {
		    			System.out.print("\n\nID or password is incorrect.\n\n");
		    		}
		    		
	    		}		    	
		    	in.close();
		    	System.out.print("Thank you. Please come again!");
	    	}
	    	catch (SQLException e) {
				System.out.print(e.getMessage());
			}
	    }
}
