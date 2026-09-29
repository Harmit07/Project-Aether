import React from 'react';
import { Handle, Position } from '@xyflow/react';

export default function CustomNode({ data }) {
    const isTargeted = data.isTargeted;

    const containerStyle = {
        background: isTargeted ? '#450a0a' : '#1e293b',
        color: isTargeted ? '#fca5a5' : '#f8fafc',
        border: isTargeted ? '2px solid #ef4444' : '1px solid #475569',
        borderRadius: '8px',
        padding: '12px 16px',
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        minWidth: '180px',
        boxShadow: isTargeted ? '0 0 20px rgba(239, 68, 68, 0.4)' : '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
        transition: 'all 0.3s ease',
        position: 'relative'
    };

    return (
        <div style={containerStyle}>
            <Handle type="target" position={Position.Top} style={{ background: 'transparent', border: 'none' }} />

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', color: isTargeted ? '#ef4444' : '#64748b' }}>
                {data.nodeType === 'server' ? (
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="2" width="20" height="8" rx="2" ry="2"></rect><rect x="2" y="14" width="20" height="8" rx="2" ry="2"></rect><line x1="6" y1="6" x2="6.01" y2="6"></line><line x1="6" y1="18" x2="6.01" y2="18"></line></svg>
                ) : (
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 8a10 10 0 0 1 16 0"></path><path d="M8 12a4 4 0 0 1 8 0"></path><line x1="12" y1="16" x2="12" y2="22"></line><line x1="10" y1="22" x2="14" y2="22"></line></svg>
                )}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '13px', fontWeight: '700', letterSpacing: '0.5px' }}>{data.label}</span>
                <span style={{ fontSize: '10px', color: isTargeted ? '#fca5a5' : '#94a3b8', textTransform: 'uppercase', fontWeight: '600' }}>
          {isTargeted ? 'Quarantined' : 'Active'}
        </span>
            </div>

            <div style={{ position: 'absolute', top: '-5px', right: '-5px', width: '12px', height: '12px', borderRadius: '50%', backgroundColor: isTargeted ? '#ef4444' : '#10b981', border: '2px solid #0f172a', boxShadow: isTargeted ? '0 0 8px #ef4444' : 'none' }}></div>

            <Handle type="source" position={Position.Bottom} style={{ background: 'transparent', border: 'none' }} />
        </div>
    );
}