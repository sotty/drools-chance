package org.kie.semantics.lang.util;

import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.EventFactHandle;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import java.util.PriorityQueue;

public class WMDumper {


	public static String reportWMObjects(KieSession session ) {
		PriorityQueue<String> q = new PriorityQueue<>();
		for (FactHandle fh : session.getFactHandles()) {
			if (fh instanceof EventFactHandle ) {
				EventFactHandle efh = (EventFactHandle) fh;
				q.add("\t " + efh.getStartTimestamp() + "\t" + efh.getObject().toString() + "\n");
			} else {
				Object o = ((DefaultFactHandle ) fh).getObject();
				q.add("\t " + o.toString() + "\n");
			}
		}
		StringBuilder ans = new StringBuilder( " ---------------- WM " + session.getObjects().size() + " --------------\n" );
		while (! q.isEmpty())
			ans.append( q.poll() );
		ans.append( " ---------------- END WM -----------\n" );
		return ans.toString();
	}

}
