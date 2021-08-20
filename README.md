# GeometricMinFilter
This is the simulation code for Paper "Super Spreader Identification using Geometric-Min Filter" published in IEEE/ACM Transcation on Networking. 
To run the code, please go to GeneralUtil.java File and 
1. change dataStreamForFlowSpread = file path to the traffic data 
the format is like follows: each line contains information of a packet, denoted as elementid+\t+flowid.
2. change dataSummaryForFlowSpread = the summary file,
the format is like follows: each line contains information of a flow, denoted as flowid+\t+spread.
3. change path = the path you want to store the result file,
the format of the result file will be like follows: each line contains information of a flow, denoted as flowid+\t+ actual spread+\t+ estimated spread.
Only the flows identified as superspreaders will have a positive estimate, all the other flows will estimated as 0.


The parameter setting can be changed in GeometricMinFilter.java
