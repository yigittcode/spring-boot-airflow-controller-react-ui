import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Layout from './components/Layout';
import DagsList from './components/DagsList';
import DagRuns from './components/DagRuns';
import TaskInstances from './components/TaskInstances';
import DagActionLogs from './components/DagActionLogs';
import Login from './components/Login';
import { ThemeProvider as MuiThemeProvider, createTheme } from '@mui/material';
import { isAuthenticated } from './utils/auth';
import { ThemeProvider, useTheme } from './contexts/ThemeContext';

function AppContent() {
  const [authenticated, setAuthenticated] = useState<boolean>(isAuthenticated());
  const { mode } = useTheme();

  // Create theme based on current mode
  const theme = createTheme({
    palette: {
      mode: mode,
    },
  });

  const handleLoginSuccess = () => {
    setAuthenticated(true);
  };

  // Check authentication status on component mount
  useEffect(() => {
    setAuthenticated(isAuthenticated());
  }, []);

  return (
    <MuiThemeProvider theme={theme}>
      <Router>
        {authenticated ? (
          <Layout onLogout={() => setAuthenticated(false)}>
            <Routes>
              <Route path="/" element={<DagsList />} />
              <Route path="/dags/:dagId/runs" element={<DagRuns />} />
              <Route path="/dags/:dagId/runs/:runId/tasks" element={<TaskInstances />} />
              <Route path="/logs" element={<DagActionLogs />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Layout>
        ) : (
          <Routes>
            <Route path="*" element={<Login onLoginSuccess={handleLoginSuccess} />} />
          </Routes>
        )}
      </Router>
    </MuiThemeProvider>
  );
}

function App() {
  return (
    <ThemeProvider>
      <AppContent />
    </ThemeProvider>
  );
}

export default App;
