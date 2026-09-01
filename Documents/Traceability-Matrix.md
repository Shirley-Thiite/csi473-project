Traceability Matrix

Requirement	                        Use Case(s)	                    Analysis Element(s)	            Verification

BR-01: Unique Reporting	            UC-02: Report Lost Item         • LostReport                    Unit tests for duplicate detection; Integration test for 24-hour window enforcement
                                    UC-03: Report Found Item	    • FoundReport
                                                                    • User	

BR-02: Status Transition Rules	    UC-04: Update Item Status       • Item                          Unit tests for transition validation; Integration tests for invalid transitions
                                    UC-06: Validate Claim	        • Status (enum)
                                                                    • ChainOfCustody
	
BR-03: Claim Restrictions	        UC-05: Submit Claim	            • Claim                         Unit test to prevent self-claim; Integration test for claim submission flow
                                                                    • Item
                                                                    • User	

BR-04: Evidence Privacy	            UC-05: Submit Claim             • Evidence                      RBAC integration tests; Security tests for private data exposure
                                    UC-07: View Item Details	    • Administrator
                                                                    • User	

BR-05: Handover Requirement	        UC-08: Approve Claim            • HandoverRecord                Scheduled job tests; Integration test for 7-day deadline enforcement
                                    UC-09: Complete Handover	    • Claim
                                                                    • Administrator	

BR-06: Authentication Required	    All protected use cases	        • User                      	Middleware integration tests; Security tests for unauthenticated access
                                                                    • Administrator

BR-07:Chain of Custody Completeness	UC-04: Update Item Status	    • ChainOfCustody                Trigger-based logging tests; Audit trail verification; Integrity tests
                                    UC-06: Validate Claim           • Item
                                    UC-08: Approve Claim            • User
                                                                    • Administrator	


BR-08:Unique Claim per Item per User	UC-05: Submit Claim	        • Claim                         Unit tests for active claim check; Integration tests for duplicate submission prevention
                                                                    • User
                                                                    • Item	

Core Req: Search Functionality	    UC-10: Search Items	            • SearchEngine                  Search algorithm tests; Manual search permission tests; Matching accuracy tests
                                                                    • Item
                                                                    • LostReport
                                                                    • FoundReport
                                                                    • Administrator	

Core Req: Notification System	    UC-05: Submit Claim             • Notification                  Unit tests for notification sending; Integration tests for claim decision notifications
                                    UC-08: Approve Claim            • User
                                    UC-11: Receive Notifications	• Claim
                                                                    • Item	

Core Req: Reporting	                UC-02: Report Lost Item         • LostReport                    Report creation integration tests; Validation tests for report fields
                                    UC-03: Report Found Item	    • FoundReport
                                                                    • Item
                                                                    • ChainOfCustody	