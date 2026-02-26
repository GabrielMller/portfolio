'use client';
import { styled } from "@mui/material";
const STATUS_COLORS = {
  OK: '#10b981',
  WARNING: '#f59e0b',
  ERROR: '#ef4444'
};

const StatusBadge = styled('div')(({ status }: { status: 'OK' | 'WARNING' | 'ERROR' }) => ({
  background: STATUS_COLORS[status],
  boxShadow: `0 0 8px ${STATUS_COLORS[status]}`,
  animation: 'pulseSoft 2s infinite',
  borderRadius: '50%',
  width: '8px',
  height: '8px',
  display: 'inline-block',
  marginRight: '8px'
}));

export default StatusBadge;