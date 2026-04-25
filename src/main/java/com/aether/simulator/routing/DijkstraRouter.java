package com.aether.simulator.routing;

import com.aether.simulator.model.Edge;
import com.aether.simulator.model.NetworkGraph;
import com.aether.simulator.model.Node;

import java.util.*;

public class DijkstraRouter {

    public static List<Node> findFastestPath(NetworkGraph graph,Node source,Node destination)
    {
        Map<Node,Integer> latencies= new HashMap<>();
        Map<Node,Node> previousNodes= new HashMap<>();

        PriorityQueue<Node> queue= new PriorityQueue<>(Comparator.comparingInt(latencies::get));

        for(Node node: graph.getAdjacencyList().keySet())
        {
            latencies.put(node,Integer.MAX_VALUE);
        }
        latencies.put(source,0);
        queue.add(source);

        while(!queue.isEmpty())
        {
            Node current = queue.poll();

            if(current.equals(destination))
            {
                break;
            }

            List<Edge> neighbors= graph.getAdjacencyList().getOrDefault(current,new ArrayList<>());

            for(Edge edge:neighbors)
            {
                Node neighbor=edge.getDestination();


                int newlatency = latencies.get(current)+ edge.getTelemetry().getLatencyMs();
                if(newlatency<latencies.get(neighbor))
                {
                    latencies.put(neighbor,newlatency);
                    previousNodes.put(neighbor,current);


                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        List<Node> path= new ArrayList<>();
        Node curr=destination;
        if(previousNodes.get(curr)!=null || curr.equals(source))
        {
            while(curr!=null)
            {
                path.add(curr);
                curr=previousNodes.get(curr);
            }
            Collections.reverse(path);
        }
        return path;

    }
}
