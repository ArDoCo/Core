package edu.kit.ipd.consistency_analyzer.modelproviders;

import java.util.List;

public class HardCodedModelInput {
	List<Triple<String, String, String>> instances;
	List<Quadruple<String, String, String, String>> relations;

	public HardCodedModelInput() {
		instances = loadInstances();
		relations = loadRelations();
	}

	public List<Triple<String, String, String>> getInstances() {
		return instances;
	}

	public List<Quadruple<String, String, String, String>> getRelations() {
		return relations;
	}

	private List<Triple<String, String, String>> loadInstances() {

		String compositeComponent = "composite component";
		String interfaceItem = "interface";
		String basicComponent = "basicComponent";

		Triple<String, String, String> i0 = new Triple<>("Common", compositeComponent, "i0");
		Triple<String, String, String> i1 = new Triple<>("Common Interface", interfaceItem, "i1");
		Triple<String, String, String> i2 = new Triple<>("util", basicComponent, "i2");
		Triple<String, String, String> i3 = new Triple<>("exception", basicComponent, "i3");
		Triple<String, String, String> i4 = new Triple<>("datatransfer", basicComponent, "i4");

		Triple<String, String, String> i5 = new Triple<>("UI", compositeComponent, "i5");
		Triple<String, String, String> i6 = new Triple<>("UI Interface", interfaceItem, "i6");
		Triple<String, String, String> i7 = new Triple<>("automated", basicComponent, "i7");
		Triple<String, String, String> i8 = new Triple<>("webapi", basicComponent, "i8");
		Triple<String, String, String> i9 = new Triple<>("website", basicComponent, "i9");
		Triple<String, String, String> i10 = new Triple<>("UI website Interface", interfaceItem, "i10");

		Triple<String, String, String> i11 = new Triple<>("Logic", compositeComponent, "i11");
		Triple<String, String, String> i12 = new Triple<>("Logic Interface", interfaceItem, "i12");
		Triple<String, String, String> i13 = new Triple<>("core", basicComponent, "i13");
		Triple<String, String, String> i14 = new Triple<>("Logic core Interface", interfaceItem, "i14");
		Triple<String, String, String> i15 = new Triple<>("api", basicComponent, "i15");
		Triple<String, String, String> i16 = new Triple<>("Logic api Interface", interfaceItem, "i16");

		Triple<String, String, String> i17 = new Triple<>("Storage", compositeComponent, "i17");
		Triple<String, String, String> i18 = new Triple<>("Storage api Interface", interfaceItem, "i18");
		Triple<String, String, String> i19 = new Triple<>("Storage entity Interface", interfaceItem, "i19");
		Triple<String, String, String> i20 = new Triple<>("entity", basicComponent, "i20");
		Triple<String, String, String> i21 = new Triple<>("api", basicComponent, "i21");
		Triple<String, String, String> i22 = new Triple<>("search", basicComponent, "i22");
		Triple<String, String, String> i23 = new Triple<>("Storage search Interface", interfaceItem, "i23");

		Triple<String, String, String> i24 = new Triple<>("Test Driver", compositeComponent, "i24");
		Triple<String, String, String> i25 = new Triple<>("driver", basicComponent, "i25");
		Triple<String, String, String> i26 = new Triple<>("Test driver Interface", interfaceItem, "i26");
		Triple<String, String, String> i27 = new Triple<>("cases", basicComponent, "i27");

		Triple<String, String, String> i28 = new Triple<>("E2E", compositeComponent, "i28");
		Triple<String, String, String> i29 = new Triple<>("pageobjects", basicComponent, "i29");
		Triple<String, String, String> i30 = new Triple<>("cases", basicComponent, "i30");
		Triple<String, String, String> i31 = new Triple<>("util", basicComponent, "i31");
		Triple<String, String, String> i32 = new Triple<>("E2E pageobjects Interface", interfaceItem, "i32");
		Triple<String, String, String> i33 = new Triple<>("E2E util Interface", interfaceItem, "i33");

		Triple<String, String, String> i34 = new Triple<>("Client", compositeComponent, "i34");
		Triple<String, String, String> i35 = new Triple<>("scripts", basicComponent, "i35");
		Triple<String, String, String> i36 = new Triple<>("remoteapi", basicComponent, "i36");
		Triple<String, String, String> i37 = new Triple<>("Client remoteapi Interface", interfaceItem, "i37");

		Triple<String, String, String> i38 = new Triple<>("GAE Datastorage", basicComponent, "i38");
		Triple<String, String, String> i39 = new Triple<>("GAE DataStore Interface", interfaceItem, "i39");

		return List.of(//
				i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20, i21, i22, //
				i23, i24, i25, i26, i27, i28, i29, i30, i31, i32, i33, i34, i35, i36, i37, i38, i39);

	}

	private List<Quadruple<String, String, String, String>> loadRelations() {

		String provide = "provide";
		String use = "use";
		String in = "in";

		// Common
		Quadruple<String, String, String, String> r0 = new Quadruple<>("i0", "i1", provide, "r0");
		Quadruple<String, String, String, String> r1 = new Quadruple<>("i2", "i0", in, "r1");
		Quadruple<String, String, String, String> r2 = new Quadruple<>("i3", "i0", in, "r2");
		Quadruple<String, String, String, String> r3 = new Quadruple<>("i4", "i0", in, "r3");
		Quadruple<String, String, String, String> r4 = new Quadruple<>("i4", "i19", use, "r4");

		// UI
		Quadruple<String, String, String, String> r5 = new Quadruple<>("i5", "i6", provide, "r5");
		Quadruple<String, String, String, String> r6 = new Quadruple<>("i9", "i10", provide, "r6");
		Quadruple<String, String, String, String> r7 = new Quadruple<>("i7", "i5", in, "r7");
		Quadruple<String, String, String, String> r8 = new Quadruple<>("i8", "i5", in, "r8");
		Quadruple<String, String, String, String> r9 = new Quadruple<>("i9", "i5", in, "r9");
		Quadruple<String, String, String, String> r10 = new Quadruple<>("i5", "i1", use, "r10");
		Quadruple<String, String, String, String> r11 = new Quadruple<>("i7", "i16", use, "r11");
		Quadruple<String, String, String, String> r12 = new Quadruple<>("i8", "i16", use, "r12");

		// Logic
		Quadruple<String, String, String, String> r13 = new Quadruple<>("i11", "i12", provide, "r13");
		Quadruple<String, String, String, String> r14 = new Quadruple<>("i13", "i14", provide, "r14");
		Quadruple<String, String, String, String> r15 = new Quadruple<>("i15", "i16", provide, "r15");
		Quadruple<String, String, String, String> r16 = new Quadruple<>("i13", "i11", in, "r16");
		Quadruple<String, String, String, String> r17 = new Quadruple<>("i15", "i11", in, "r17");
		Quadruple<String, String, String, String> r18 = new Quadruple<>("i11", "i1", use, "r18");
		Quadruple<String, String, String, String> r19 = new Quadruple<>("i15", "i14", use, "r19");
		Quadruple<String, String, String, String> r20 = new Quadruple<>("i14", "i18", use, "r20");

		// Storage
		Quadruple<String, String, String, String> r21 = new Quadruple<>("i20", "i19", provide, "r21");
		Quadruple<String, String, String, String> r22 = new Quadruple<>("i21", "i18", provide, "r22");
		Quadruple<String, String, String, String> r23 = new Quadruple<>("i22", "i23", provide, "r23");
		Quadruple<String, String, String, String> r24 = new Quadruple<>("i20", "i17", in, "r24");
		Quadruple<String, String, String, String> r25 = new Quadruple<>("i21", "i17", in, "r25");
		Quadruple<String, String, String, String> r26 = new Quadruple<>("i22", "i17", in, "r26");
		Quadruple<String, String, String, String> r27 = new Quadruple<>("i17", "i1", use, "r27");
		Quadruple<String, String, String, String> r28 = new Quadruple<>("i21", "i19", use, "r28");
		Quadruple<String, String, String, String> r29 = new Quadruple<>("i21", "i23", use, "r29");
		Quadruple<String, String, String, String> r30 = new Quadruple<>("i21", "i39", use, "r30");
		Quadruple<String, String, String, String> r31 = new Quadruple<>("i22", "i18", use, "r31");

		// Test Driver
		Quadruple<String, String, String, String> r32 = new Quadruple<>("i25", "i26", provide, "r32");
		Quadruple<String, String, String, String> r33 = new Quadruple<>("i25", "i24", in, "r33");
		Quadruple<String, String, String, String> r34 = new Quadruple<>("i27", "i24", in, "r34");
		Quadruple<String, String, String, String> r35 = new Quadruple<>("i27", "i26", use, "r25");
		Quadruple<String, String, String, String> r36 = new Quadruple<>("i27", "i10", use, "r36");

		// E2E
		Quadruple<String, String, String, String> r37 = new Quadruple<>("i29", "i32", provide, "r37");
		Quadruple<String, String, String, String> r38 = new Quadruple<>("i31", "i33", provide, "r38");
		Quadruple<String, String, String, String> r39 = new Quadruple<>("i29", "i28", in, "r39");
		Quadruple<String, String, String, String> r40 = new Quadruple<>("i30", "i28", in, "r40");
		Quadruple<String, String, String, String> r41 = new Quadruple<>("i31", "i28", in, "r41");
		Quadruple<String, String, String, String> r42 = new Quadruple<>("i29", "i10", use, "r42");
		Quadruple<String, String, String, String> r43 = new Quadruple<>("i30", "i29", use, "r43");
		Quadruple<String, String, String, String> r44 = new Quadruple<>("i30", "i31", use, "r44");

		// Client
		Quadruple<String, String, String, String> r45 = new Quadruple<>("i36", "i37", provide, "r45");
		Quadruple<String, String, String, String> r46 = new Quadruple<>("i35", "i34", in, "r46");
		Quadruple<String, String, String, String> r47 = new Quadruple<>("i36", "i34", in, "r47");
		Quadruple<String, String, String, String> r48 = new Quadruple<>("i35", "i37", use, "r48");
		Quadruple<String, String, String, String> r49 = new Quadruple<>("i36", "i39", use, "r49");

		// GAE Datastore
		Quadruple<String, String, String, String> r50 = new Quadruple<>("i38", "i39", provide, "r50");

		return List.of(//
				r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, //
				r23, r24, r25, r26, r27, r28, r29, r30, r31, r32, r33, r34, r35, r36, r37, r38, r39, r40, r41, r42, r43, r44, r45, r46, r47, r48, r49, r50);

	}

	public class Triple<T, U, V> {

		private final T first;
		private final U second;
		private final V third;

		public Triple(T first, U second, V third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}

		public T getFirst() {
			return first;
		}

		public U getSecond() {
			return second;
		}

		public V getThird() {
			return third;
		}
	}

	public class Quadruple<T, U, V, R> {

		private final T first;
		private final U second;
		private final V third;
		private final R fourth;

		public Quadruple(T first, U second, V third, R fourth) {
			this.first = first;
			this.second = second;
			this.third = third;
			this.fourth = fourth;
		}

		public T getFirst() {
			return first;
		}

		public U getSecond() {
			return second;
		}

		public V getThird() {
			return third;
		}

		public R getFourth() {
			return fourth;
		}
	}
}
