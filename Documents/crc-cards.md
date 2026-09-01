CRC Card 1: Item
Aspect	                        Details

Class Name	                    Item

Responsibilities	            • Track its own status and location
                                • Manage private evidence
                                • Update status when validated
                                • Maintain chain of custody entries

Collaborators	                • Status
                                • Evidence
                                • ChainOfCustody
                                • LostReport
                                • FoundReport

Rationale	                    Item is the central entity and should own its lifecycle data to maintain cohesion

CRC Card 2: Claim
Aspect	                        Details

Class Name	                    Claim

Responsibilities	            • Manage claim lifecycle (submission, approval, rejection)
                                • Validate ownership proof
                                • Handle status transitions
                                • Notify claimant of decisions

Collaborators	                • Item
                                • User
                                • Evidence
                                • HandoverRecord
                                • Notification

Rationale	                    Claim owns its approval/rejection process to maintain data integrity

CRC Card 3: ChainOfCustody
Aspect	                        Details

Class Name	                    ChainOfCustody

Responsibilities	            • Record immutable audit trail of all status changes
                                • Maintain actor identity and timestamps
                                • Provide complete history for an item
                                • Ensure tamper-proof records

Collaborators	                • Item
                                • User
                                • Status

Rationale	                    Separate class for auditing concerns maintains separation of concerns and supports immutability

CRC Card 4: User
Aspect	                        Details

Class Name	                    User

Responsibilities	            • Authenticate using credentials
                                • Manage profile information
                                • Report items lost or found
                                • Submit claims for found items
                                • Receive notifications

Collaborators	                • LostReport
                                • FoundReport
                                • Claim
                                • Notification
                                • Role

Rationale	                    User is the primary actor and owns its actions and interactions with the system

CRC Card 5: FoundReport
Aspect	                        Details

Class Name	                    FoundReport

Responsibilities	            • Create new found item record
                                • Validate required fields
                                • Set initial status to FOUND
                                • Initiate chain of custody entry
                                • Generate unique report ID

Collaborators	                • Item
                                • ChainOfCustody
                                • User (reporter)

Rationale	                    FoundReport specializes the reporting process with specific found item workflows

CRC Card 6: Administrator
Aspect	                        Details

Class Name	                    Administrator (extends User)

Responsibilities	            • Review and approve/reject claims
                                • Manually search database
                                • Update item statuses
                                • Create handover records
                                • Manage system configuration (categories)

Collaborators	                • Claim
                                • Item
                                • HandoverRecord
                                • ChainOfCustody
                                • Category

Rationale	                    Administrator role has elevated privileges and specific responsibilities that extend base User capabilities

CRC Card 7: Notification
Aspect	                        Details

Class Name	                    Notification

Responsibilities	            • Send alerts for potential matches
                                • Notify claim decisions
                                • Track read/unread status
                                • Manage notification types

Collaborators	                • User
                                • Claim
                                • Item

Rationale	                    Notification is responsible for user communication and should be decoupled from business logic

CRC Card 8: SearchEngine
Aspect	                        Details

Class Name	                    SearchEngine

Responsibilities	            • Match lost and found items
                                • Generate potential matches
                                • Filter search results
                                • Support administrative manual search

Collaborators	                • Item
                                • LostReport
                                • FoundReport
                                • Administrator
                                
Rationale	                    Search functionality is separate from item management to support scalability and maintainability

