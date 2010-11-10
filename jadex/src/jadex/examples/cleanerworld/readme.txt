Cleanerworld
--------------

The single agent version can be started
by directly launching the Clearner.agent.xml
in the single subdirectory.

To start the multi agent example application launch
the manager agent in the multi directory.
The multi version of the cleanerworld scenario allows
to use an arbitrary number of cleaners in one environment.
Additionally the system can be easily distributed over
different hosts.

Note that 3 different kinds of agents
exist is the multi version: The environment agent manages
an enironment object. A cleaner agent uses also
an environment object (a local proxy) to enact in it.
An environment proxy agent forwards the actions made
by the cleaner to the environment agent. The
communication between proxy and environment agents
is based on a cleanerworld ontology.

Overall task:
In a simulated environment cleaner agents are used
to perform several tasks.
1. They have explore their environment to gain info
about charging stations, waste bin and waste pieces.
2. They have to clean-up waste pieces and bring
them to a waste bin nearby.
3. They have to guard the environment at night time
by doing patrols.



