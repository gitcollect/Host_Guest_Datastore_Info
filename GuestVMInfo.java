package edu.sjsu.cmpe.cmpe283;

/*Import statements*/
import java.net.URL;

import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * This class captures datastore and network information of the 
 * host OS, along with the configuration details of the multiple 
 * guest OS running on the host OS.
 * 
 * Author: Neha Viswanathan, 010029097
 * Date: 03-01-2015
 */

public class Hw1_097_Cmpe283_Neha {

	public static void main(String[] args) throws Exception {
		ServiceInstance sInst = null;
		try {
			//Instantiating ServiceInstance using the host IP and root id and password. 
			sInst = new ServiceInstance(new URL("https://192.168.23.129/sdk"), "root",
					"neha123", true);
			Folder rootFolder = sInst.getRootFolder();

			/*Retrieving host information*/
			ManagedEntity[] meHost = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
			if (meHost == null || meHost.length == 0) {
				return;
			}
			for(int hostCount=0; hostCount<meHost.length; hostCount++) {
				HostSystem host = (HostSystem) meHost[hostCount];
				System.out.println("Host[" + hostCount + "]");
				System.out.println("Host = " + host.getName());
				System.out.println("ProductFullName = " + sInst.getAboutInfo().getFullName());
				
				/*Retrieving Datastore information from the host*/
				Datastore[] ds = host.getDatastores();
				if(ds != null && ds.length !=0) {
					int dsCount = 0;
					for(Datastore datas : ds) {
						System.out.println("Datastore["+dsCount+"]");
						System.out.println("name = " + datas.getSummary().getName() +
								" capacity = " + datas.getSummary().getCapacity() + 
								" FreeSpace = " + datas.getSummary().getFreeSpace());
						dsCount++;
					}
				}

				/*Retrieving Network information from the host*/
				Network[] nw = host.getNetworks();
				if(nw != null && nw.length !=0) {
					int nwCount = 0;
					for(Network ntw : nw) {
						System.out.println("Network["+nwCount+"]");
						System.out.println("name = " + ntw.getName());
						nwCount++;
					}
				}
			}

			/*Retrieving guest information*/
			ManagedEntity[] meGuest = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
			Task task;
			if (meGuest == null || meGuest.length == 0) {
				return;
			}
			for(int gCount=0; gCount<meGuest.length; gCount++) {

				VirtualMachine vm = (VirtualMachine) meGuest[gCount];
				VirtualMachineConfigInfo vminfo = vm.getConfig();

				System.out.println("VM[" + gCount + "]");
				System.out.println("Name = " + vm.getName());
				System.out.println("GuestOS = " + vminfo.getGuestFullName());
				GuestInfo gi = vm.getGuest();
				System.out.println("Guest state = " + gi.getGuestState());
				VirtualMachineRuntimeInfo vmrinfo = vm.getRuntime();
				System.out.println("Power state = " + vmrinfo.getPowerState());

				/*Transition of power states between the two guest OS
				1. Power off the VM if it is powered on */
				if(vmrinfo.getPowerState().toString().equalsIgnoreCase("poweredOn")) {
					task = vm.powerOffVM_Task();
					task.waitForTask();
					if(task.getTaskInfo().getState() != null) {
						System.out.println("Power off VM: status = " + task.getTaskInfo().getState());
					}
					else {
						System.err.println("Failure while Power Off!");
					}
				}
				/*2. Power on the VM if it is powered off*/
				else {
					task = vm.powerOnVM_Task(null);
					task.waitForTask();
					if(task.getTaskInfo().getState() != null) {
						System.out.println("Power on VM: status = " + task.getTaskInfo().getState());
					}
					else {
						System.err.println("Failure while Power On!");
					}
				}

				/*Get a list of recent tasks on each VM*/
				Task[] taskArr = meGuest[gCount].getRecentTasks();
				for(Task tsk : taskArr) {
					System.out.println("task: target = " + vm.getName() + 
							" op = " + tsk.getTaskInfo().getName() + 
							" startTime = " + tsk.getTaskInfo().getStartTime().getTime());
				}
			}
		}
		/*Catch exception if any*/
		catch(Exception e) {
			System.err.println("Exception occurred :: " + e.getMessage());
		}
		/*Logout of connection*/
		finally {
			if(sInst != null) {
				System.out.println("Logging out!");
				sInst.getServerConnection().logout();
			}
		}
	}
}
