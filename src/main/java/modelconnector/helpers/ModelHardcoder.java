package modelconnector.helpers;

import java.util.ArrayList;
import java.util.List;

import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.modelExtractor.state.Relation;

/**
 * This helper class contains hard coded models, that can be loaded.
 *
 * @author Sophie
 *
 */
public final class ModelHardcoder {

	private ModelHardcoder() {
		throw new IllegalAccessError();
	}

	public static ModelExtractionState getEmptyExtractionState() {
		return new ModelExtractionState(new ArrayList<Instance>(), new ArrayList<Relation>());
	}

	/**
	 * Simulates the read in of the TEAMMATES model.
	 *
	 * @return the resulting extraction state.
	 */
	public static ModelExtractionState hardCodeExtractionStateOfTeammates() {
		Instance i0 = new Instance("Common", "composite component", 0);
		Instance i1 = new Instance("Common Interface", "interface", 1);
		Instance i2 = new Instance("util", "basic component", 2);
		Instance i3 = new Instance("exception", "basic component", 3);
		Instance i4 = new Instance("datatransfer", "basic component", 4);

		Instance i5 = new Instance("UI", "composite component", 5);
		Instance i6 = new Instance("UI Interface", "interface", 6);
		Instance i7 = new Instance("automated", "basic component", 7);
		Instance i8 = new Instance("webapi", "basic component", 8);
		Instance i9 = new Instance("website", "basic component", 9);
		Instance i10 = new Instance("UI website Interface", "interface", 10);

		Instance i11 = new Instance("Logic", "composite component", 11);
		Instance i12 = new Instance("Logic Interface", "interface", 12);
		Instance i13 = new Instance("core", "basic component", 13);
		Instance i14 = new Instance("Logic core Interface", "interface", 14);
		Instance i15 = new Instance("api", "basic component", 15);
		Instance i16 = new Instance("Logic api Interface", "interface", 16);

		Instance i17 = new Instance("Storage", "composite component", 17);
		Instance i18 = new Instance("Storage api Interface", "interface", 18);
		Instance i19 = new Instance("Storage entity Interface", "interface", 19);
		Instance i20 = new Instance("entity", "basic component", 20);
		Instance i21 = new Instance("api", "basic component", 21);
		Instance i22 = new Instance("search", "basic component", 22);
		Instance i23 = new Instance("Storage search Interface", "interface", 23);

		Instance i24 = new Instance("Test Driver", "composite component", 24);
		Instance i25 = new Instance("driver", "basic component", 25);
		Instance i26 = new Instance("Test driver Interface", "interface", 26);
		Instance i27 = new Instance("cases", "basic component", 27);

		Instance i28 = new Instance("E2E", "composite component", 28);
		Instance i29 = new Instance("pageobjects", "basic component", 29);
		Instance i30 = new Instance("cases", "basic component", 30);
		Instance i31 = new Instance("util", "basic component", 31);
		Instance i32 = new Instance("E2E pageobjects Interface", "interface", 32);
		Instance i33 = new Instance("E2E util Interface", "interface", 33);

		Instance i34 = new Instance("Client", "composite component", 34);
		Instance i35 = new Instance("scripts", "basic component", 35);
		Instance i36 = new Instance("remoteapi", "basic component", 36);
		Instance i37 = new Instance("Client remoteapi Interface", "interface", 37);

		Instance i38 = new Instance("GAE Datastorage", "basic component", 38);
		Instance i39 = new Instance("GAE DataStore Interface", "Interface", 39);

		// Common
		Relation r0 = new Relation(i0, i1, "provide", 0);
		Relation r1 = new Relation(i2, i0, "in", 1);
		Relation r2 = new Relation(i3, i0, "in", 2);
		Relation r3 = new Relation(i4, i0, "in", 3);
		Relation r4 = new Relation(i4, i19, "use", 4);

		// UI
		Relation r5 = new Relation(i5, i6, "provide", 5);
		Relation r6 = new Relation(i9, i10, "provide", 6);
		Relation r7 = new Relation(i7, i5, "in", 7);
		Relation r8 = new Relation(i8, i5, "in", 8);
		Relation r9 = new Relation(i9, i5, "in", 9);
		Relation r10 = new Relation(i5, i1, "use", 10);
		Relation r11 = new Relation(i7, i16, "use", 11);
		Relation r12 = new Relation(i8, i16, "use", 12);

		// Logic
		Relation r13 = new Relation(i11, i12, "provide", 13);
		Relation r14 = new Relation(i13, i14, "provide", 14);
		Relation r15 = new Relation(i15, i16, "provide", 15);
		Relation r16 = new Relation(i13, i11, "in", 16);
		Relation r17 = new Relation(i15, i11, "in", 17);
		Relation r18 = new Relation(i11, i1, "use", 18);
		Relation r19 = new Relation(i15, i14, "use", 19);
		Relation r20 = new Relation(i14, i18, "use", 20);

		// Storage
		Relation r21 = new Relation(i20, i19, "provide", 21);
		Relation r22 = new Relation(i21, i18, "provide", 22);
		Relation r23 = new Relation(i22, i23, "provide", 23);
		Relation r24 = new Relation(i20, i17, "in", 24);
		Relation r25 = new Relation(i21, i17, "in", 25);
		Relation r26 = new Relation(i22, i17, "in", 26);
		Relation r27 = new Relation(i17, i1, "use", 27);
		Relation r28 = new Relation(i21, i19, "use", 28);
		Relation r29 = new Relation(i21, i23, "use", 29);
		Relation r30 = new Relation(i21, i39, "use", 30);
		Relation r31 = new Relation(i22, i18, "use", 31);

		// Test Driver
		Relation r32 = new Relation(i25, i26, "provide", 32);
		Relation r33 = new Relation(i25, i24, "in", 33);
		Relation r34 = new Relation(i27, i24, "in", 34);
		Relation r35 = new Relation(i27, i26, "use", 25);
		Relation r36 = new Relation(i27, i10, "use", 36);

		// E2E
		Relation r37 = new Relation(i29, i32, "provide", 37);
		Relation r38 = new Relation(i31, i33, "provide", 38);
		Relation r39 = new Relation(i29, i28, "in", 39);
		Relation r40 = new Relation(i30, i28, "in", 40);
		Relation r41 = new Relation(i31, i28, "in", 41);
		Relation r42 = new Relation(i29, i10, "use", 42);
		Relation r43 = new Relation(i30, i29, "use", 43);
		Relation r44 = new Relation(i30, i31, "use", 44);

		// Client
		Relation r45 = new Relation(i36, i37, "provide", 45);
		Relation r46 = new Relation(i35, i34, "in", 46);
		Relation r47 = new Relation(i36, i34, "in", 47);
		Relation r48 = new Relation(i35, i37, "use", 48);
		Relation r49 = new Relation(i36, i39, "use", 49);

		// GAE Datastore
		Relation r50 = new Relation(i38, i39, "provide", 50);

		List<Instance> instances = List.of(//
				i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14, i15, i16, i17, i18, i19, i20, i21, i22, //
				i23, i24, i25, i26, i27, i28, i29, i30, i31);
		List<Relation> relations = List.of(//
				r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, //
				r23, r24, r25, r26, r27, r28, r29, r30, r31, r32, r33, r34, r35, r36, r37, r38, r39, r40, r41, r42, r43, r44, r45, r46, r47, r48, r49, r50);

		return new ModelExtractionState(instances, relations);
	}

}
