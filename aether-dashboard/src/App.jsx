import React, { useEffect, useState } from 'react';
import { ReactFlow, Background, Controls, useNodesState, useEdgesState } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

import CustomNode from './CustomNode';
import { motion } from 'framer-motion';

const nodeTypes = { customNode: CustomNode };

const getEdgeVisuals = (latency) => {
    if (latency >= 9999) {
        return { color: '#7f1d1d', width: 4, label: 'BLOCKED', animated: false, bgOpacity: 1 };
    } else if (latency > 100) {
        return { color: '#ef4444', width: 3, label: `${latency}ms`, animated: true, bgOpacity: 0.9 };
    } else if (latency > 40) {
        return { color: '#f59e0b', width: 2, label: `${latency}ms`, animated: true, bgOpacity: 0.8 };
    } else {
        return { color: '#10b981', width: 2, label: `${latency}ms`, animated: true, bgOpacity: 0.8 };
    }
};

const createEdge = (id, source, target, initialLatency = 20) => {
    const visuals = getEdgeVisuals(initialLatency);
    return {
        id,
        source,
        target,
        animated: visuals.animated,
        label: visuals.label,
        style: { stroke: visuals.color, strokeWidth: visuals.width, transition: 'all 0.5s ease' },
        labelStyle: { fill: visuals.color, fontWeight: 700, fontSize: 11, fontFamily: 'system-ui' },
        labelBgStyle: { fill: '#0f172a', fillOpacity: visuals.bgOpacity, rx: 4, ry: 4 },
        labelBgPadding: [4, 2],
        data: { latency: initialLatency }
    };
};

const initialNodes = [
    { id: 'Tower_A', type: 'customNode', position: { x: 300, y: 50 }, data: { label: 'Data Center A', nodeType: 'server', isTargeted: false } },
    { id: 'Tower_B', type: 'customNode', position: { x: 100, y: 180 }, data: { label: 'Tower B', nodeType: 'tower', isTargeted: false } },
    { id: 'Tower_C', type: 'customNode', position: { x: 500, y: 180 }, data: { label: 'Tower C', nodeType: 'tower', isTargeted: false } },
    { id: 'Tower_D', type: 'customNode', position: { x: 300, y: 310 }, data: { label: 'Tower D', nodeType: 'tower', isTargeted: false } },
    { id: 'Tower_E', type: 'customNode', position: { x: -100, y: 180 }, data: { label: 'Tower E', nodeType: 'tower', isTargeted: false } },
    { id: 'Tower_F', type: 'customNode', position: { x: 700, y: 180 }, data: { label: 'Tower F', nodeType: 'tower', isTargeted: false } },
    { id: 'Tower_G', type: 'customNode', position: { x: 100, y: 440 }, data: { label: 'Tower G', nodeType: 'tower', isTargeted: false } },
    { id: 'Tower_H', type: 'customNode', position: { x: 500, y: 440 }, data: { label: 'Tower H', nodeType: 'tower', isTargeted: false } },
    { id: 'Tower_I', type: 'customNode', position: { x: 300, y: 440 }, data: { label: 'Tower I', nodeType: 'tower', isTargeted: false } },
    { id: 'Tower_J', type: 'customNode', position: { x: 300, y: 570 }, data: { label: 'Data Center J', nodeType: 'server', isTargeted: false } },
];

const initialEdges = [
    createEdge('eA-B', 'Tower_A', 'Tower_B'),
    createEdge('eA-C', 'Tower_A', 'Tower_C'),
    createEdge('eB-D', 'Tower_B', 'Tower_D'),
    createEdge('eC-D', 'Tower_C', 'Tower_D'),
    createEdge('eB-E', 'Tower_B', 'Tower_E'),
    createEdge('eC-F', 'Tower_C', 'Tower_F'),
    createEdge('eB-G', 'Tower_B', 'Tower_G'),
    createEdge('eC-H', 'Tower_C', 'Tower_H'),
    createEdge('eD-I', 'Tower_D', 'Tower_I'),
    createEdge('eG-I', 'Tower_G', 'Tower_I'),
    createEdge('eH-I', 'Tower_H', 'Tower_I'),
    createEdge('eI-J', 'Tower_I', 'Tower_J'),
];

export default function App() {
    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

    const [systemStatus, setSystemStatus] = useState("Monitoring");
    const [latestCommand, setLatestCommand] = useState(null);
    const [logs, setLogs] = useState([]);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8080/ws-network');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            debug: () => {},
            onConnect: () => {
                setSystemStatus("Connected & Active");

                stompClient.subscribe('/topic/ai-commands', (message) => {
                    const aiCommand = JSON.parse(message.body);
                    setLatestCommand(aiCommand);
                    setSystemStatus("Remediation Deployed");

                    const newLogEntry = {
                        id: Date.now(),
                        timestamp: new Date().toLocaleTimeString(),
                        ...aiCommand
                    };
                    setTimeout(() => {
                        setNodes((nds) => nds.map((node) => ({
                            ...node,
                            data: { ...node.data, isTargeted: false }
                        })));
                    }, 10000);
                    setLogs((prevLogs) => [newLogEntry, ...prevLogs]);

                    const target = aiCommand.targetNode || "";

                    setNodes((nds) => nds.map((node) => {
                        if (target.includes(node.id)) {
                            return { ...node, data: { ...node.data, isTargeted: true } };
                        }
                        return { ...node, data: { ...node.data, isTargeted: false } };
                    }));

                    setEdges((eds) => eds.map((edge) => {
                        const isEdgeTargeted = (target.includes(edge.source) && target.includes(edge.target)) || target === edge.source || target === edge.target;

                        if (isEdgeTargeted) {
                            const newLatency = aiCommand.newEnforcedLatency;
                            const visuals = getEdgeVisuals(newLatency);
                            return {
                                ...edge,
                                animated: visuals.animated,
                                label: visuals.label,
                                style: { stroke: visuals.color, strokeWidth: visuals.width, transition: 'all 0.5s ease' },
                                labelStyle: { fill: visuals.color, fontWeight: 700, fontSize: 11, fontFamily: 'system-ui' },
                                labelBgStyle: { fill: '#0f172a', fillOpacity: visuals.bgOpacity, rx: 4, ry: 4 },
                                data: { latency: newLatency }
                            };
                        }
                        return edge;
                    }));
                });
            },
            onDisconnect: () => setSystemStatus("Disconnected"),
        });

        stompClient.activate();
        return () => stompClient.deactivate();
    }, []);

    return (
        <div style={{ width: '100%', height: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#0f172a', color: '#f8fafc', fontFamily: 'system-ui, -apple-system, sans-serif' }}>

            <header style={{ padding: '16px 32px', backgroundColor: '#1e293b', borderBottom: '1px solid #334155', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)', zIndex: 10 }}>
                <div>
                    <h1 style={{ margin: 0, fontSize: '22px', fontWeight: '600', letterSpacing: '-0.5px' }}>Aether</h1>
                    <p style={{ margin: '2px 0 0 0', fontSize: '13px', color: '#94a3b8' }}>Autonomous Network Controller</p>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', fontWeight: '600', letterSpacing: '0.5px', textTransform: 'uppercase', color: systemStatus.includes("Active") ? '#10b981' : '#f59e0b' }}>
                    <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: systemStatus.includes("Active") ? '#10b981' : '#f59e0b', boxShadow: systemStatus.includes("Active") ? '0 0 8px #10b981' : 'none' }}></div>
                    {systemStatus}
                </div>
            </header>

            <div style={{ padding: latestCommand ? '20px 32px' : '12px 32px', borderBottom: '1px solid #334155', backgroundColor: '#0f172a', transition: 'all 0.3s ease', zIndex: 10 }}>
                {!latestCommand ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#475569' }}>
                        <div style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#475569' }}></div>
                        <p style={{ margin: 0, fontSize: '14px', fontStyle: 'italic', fontWeight: '500' }}>System operating optimally. Awaiting anomalies...</p>
                    </div>
                ) : (
                    <div style={{ width: '100%', display: 'flex', gap: '16px' }}>
                        <div style={{ flex: 1, backgroundColor: '#1e293b', padding: '16px', borderRadius: '6px', border: '1px solid #334155' }}>
                            <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '1px', fontWeight: '700' }}>Target Affected</span>
                            <p style={{ margin: '6px 0 0 0', fontWeight: '500', fontSize: '15px', color: '#ef4444' }}>{latestCommand.targetNode}</p>
                        </div>
                        <div style={{ flex: 1, backgroundColor: '#1e293b', padding: '16px', borderRadius: '6px', border: '1px solid #334155' }}>
                            <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '1px', fontWeight: '700' }}>Action Taken</span>
                            <p style={{ margin: '6px 0 0 0', fontWeight: '500', fontSize: '15px' }}>{latestCommand.action} <span style={{color: '#94a3b8'}}>({latestCommand.newEnforcedLatency}ms)</span></p>
                        </div>
                        <div style={{ flex: 2, backgroundColor: '#1e293b', padding: '16px', borderRadius: '6px', border: '1px solid #334155' }}>
                            <span style={{ fontSize: '11px', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '1px', fontWeight: '700' }}>AI Justification</span>
                            <p style={{ margin: '6px 0 0 0', fontSize: '14px', color: '#cbd5e1', lineHeight: '1.5' }}>{latestCommand.humanReadableJustification}</p>
                        </div>
                    </div>
                )}
            </div>

            <div style={{ flexGrow: 1, display: 'flex', overflow: 'hidden' }}>

                <div style={{ flex: 3, position: 'relative' }}>
                    <ReactFlow
                        nodes={nodes}
                        edges={edges}
                        onNodesChange={onNodesChange}
                        onEdgesChange={onEdgesChange}
                        nodeTypes={nodeTypes}
                        fitView
                        colorMode="dark"
                        proOptions={{ hideAttribution: true }}
                    >
                        <Background color="#334155" gap={24} size={1.5} />
                        <Controls style={{ backgroundColor: '#1e293b', fill: '#f8fafc', border: '1px solid #334155', borderRadius: '6px' }} />
                    </ReactFlow>
                </div>

                <div style={{ width: '380px', backgroundColor: '#0f172a', borderLeft: '1px solid #334155', display: 'flex', flexDirection: 'column' }}>
                    <div style={{ padding: '16px 20px', borderBottom: '1px solid #334155', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ fontSize: '12px', fontWeight: '700', letterSpacing: '1px', color: '#94a3b8' }}>LIVE AUDIT TRAIL</span>
                        <div style={{ backgroundColor: '#1e293b', color: '#3b82f6', fontSize: '11px', padding: '2px 8px', borderRadius: '12px', fontWeight: '600' }}>{logs.length} Events</div>
                    </div>

                    <div style={{ flexGrow: 1, overflowY: 'auto', padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        {logs.length === 0 ? (
                            <p style={{ fontSize: '13px', color: '#475569', fontStyle: 'italic', margin: 0, textAlign: 'center', marginTop: '20px' }}>No events recorded yet.</p>
                        ) : (
                            logs.map((log) => (
                                    <motion.div
                                        key={log.id}
                                        initial={{ opacity: 0, y: -25, scale: 0.95 }}
                                        animate={{ opacity: 1, y: 0, scale: 1 }}
                                        transition={{ duration: 0.4, type: "spring", bounce: 0.4 }}
                                        style={{ backgroundColor: '#1e293b', border: '1px solid #334155', borderRadius: '8px', padding: '16px', position: 'relative' }}
                                    >
                                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                            <span style={{ fontSize: '11px', color: '#64748b', fontWeight: '600' }}>{log.timestamp}</span>
                                            <span style={{ fontSize: '11px', color: '#ef4444', fontWeight: '700', backgroundColor: '#450a0a', padding: '2px 6px', borderRadius: '4px' }}>{log.targetNode}</span>
                                        </div>
                                        <div style={{ fontSize: '14px', fontWeight: '600', color: '#f8fafc', marginBottom: '8px' }}>
                                            {log.action} <span style={{ color: '#94a3b8', fontWeight: '400' }}>({log.newEnforcedLatency}ms)</span>
                                        </div>
                                        <div style={{ fontSize: '13px', color: '#cbd5e1', lineHeight: '1.5' }}>
                                            {log.humanReadableJustification}
                                        </div>
                                    </motion.div>
                                ))
                        )}
                    </div>
                </div>

            </div>
        </div>
    );
}