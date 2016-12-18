package Messanger.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import Messanger.Model.User;

/**
 * Advanced token ring algorithm for large networks. When # clients is too much,
 * we have to divide one ring to two rings. Each ring has one bus.
 * 
 * @author RAVEN
 *
 */
public class AdvancedTokenRingAlgorithm {

	/* previous list for only one big ring */
	private static LinkedList<User> largeRing;

	/*
	 * list of ring lists. At the beginnig it is empty. Then it has two rings.
	 * There could be more if network size will still be increasing
	 */
	private static LinkedList<LinkedList<User>> listOfRings = new LinkedList<LinkedList<User>>();

	public AdvancedTokenRingAlgorithm(LinkedList<User> largeRing) {
		this.largeRing = largeRing;
	}

	/**
	 * 
	 * @param usernameIndex
	 *            - index of user which actually has bus and which is waiting to
	 *            send its forward
	 */
	public void createNewRing(int usernameIndex) {

		/*
		 * divide largeRing to smaller rings, according to 'usernameIndex' This
		 * user should be in the middle of one of the ring
		 */
		int largeRingSize = largeRing.size();

		/* calculate new rings ranges */
		int first_ring_start;
		int first_ring_end;
		int first_ring_size = 0;
		
		if ((largeRingSize/2) % 2 == 0) {
			/* size of each ring will be %2=0*/
			first_ring_start = usernameIndex - ((largeRingSize / 2 - 1)) / 2 - 1;
			first_ring_end = usernameIndex + ((largeRingSize / 2 - 1)) / 2 ;
			
		} else {
			/* [0...5...14] ; [2...5...8] [9...12...1]] */
			first_ring_start = usernameIndex - ((largeRingSize / 2)) / 2;
			first_ring_end = usernameIndex + ((largeRingSize / 2)) / 2 ;
		}
		
		if (first_ring_start < 0)
			first_ring_start = largeRingSize + first_ring_start;
		if (first_ring_end > largeRingSize)
			first_ring_end = first_ring_end - largeRingSize;
		
		int second_ring_start = first_ring_end + 1;
		int second_ring_end = first_ring_start - 1;

		if (second_ring_end < 0)
			second_ring_end = largeRingSize + first_ring_start;

		System.out.println("first_ring_start = " + first_ring_start + " ,first_ring_end = " + first_ring_end
				+ " ,second_ring_start = " + second_ring_start + " ,second_ring_end = " + second_ring_end);

		/*
		 * first ring: [first_ring_start...usernameIndex...first_ring_end]
		 * second ring: [second_ring_start......second_ring_end] where
		 * first_ring_start, first_ring_end, first_ring_end, second_ring_end are
		 * from this large ring
		 */

		/*
		 * create new ring and copy users from calculate ranges
		 */

		LinkedList<User> newRing = new LinkedList<User>();
		
		System.out.println("newRing size = " + newRing.size());
		System.out.println("sholud be size = " + first_ring_size);

		for(int i = first_ring_start ; ; i++) {
			if( i == first_ring_end ) {
				newRing.add(largeRing.get(i));
				break;
			}
			if( i >= largeRingSize )
				i = 0;
			newRing.add(largeRing.get(i));
		}
		
		
		/*
		 * delete users from big ring according to the new range
		 * it will create the second ring, but smaller the previously
		*/
		largeRing.removeAll(newRing);

		newRing.add(largeRing.get(0));

		/* display new rings */
		
		System.out.println("newRing size = " + newRing.size());
		System.out.println("oldRing size = " + largeRing.size());
		
		System.out.println("newRing = ");
		displayRing(newRing);
		System.out.println("oldRing = " );
		displayRing(largeRing);
		
		/* add rings to list of rings */
		listOfRings.add(newRing);
		listOfRings.add(largeRing);
	}
	
	public void displayRing(LinkedList<User> ring) {
		for(int i = 0 ; i < ring.size() ; i++ ){
			System.out.print(ring.get(i).getId() + ",");
		}
		System.out.println();
	}
	public static LinkedList<User> generateExampleList(int size) {
		LinkedList<User> testLargeRing = new LinkedList<User>();
		User user;
		for(int i = 0 ; i < size ; i++) {
			user = new User();
			user.setId(i);
			testLargeRing.add(user);
		}
		
		return testLargeRing;
	}

	public static void main(String[] args) {
		
		/* generate test large ring */
		LinkedList<User> testLargeRing = generateExampleList(11);

		AdvancedTokenRingAlgorithm advancedTokenRingAlgorithm = new AdvancedTokenRingAlgorithm(testLargeRing);

		/* divide this ring to two smaller */
		advancedTokenRingAlgorithm.createNewRing(1);
		
	}


	public LinkedList<LinkedList<User>> getListOfRings() {
		return listOfRings;
	}

	public void setListOfRings(LinkedList<LinkedList<User>> listOfRings) {
		AdvancedTokenRingAlgorithm.listOfRings = listOfRings;
	}
	
	

}
