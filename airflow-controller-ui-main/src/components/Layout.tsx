import { Box, CssBaseline, AppBar, Toolbar, Typography, Drawer, Button, Stack, IconButton, Tooltip } from '@mui/material';
import { Logout, Person, Brightness4, Brightness7 } from '@mui/icons-material';
import { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { clearCredentials, getCredentials } from '../utils/auth';
import { useTheme } from '../contexts/ThemeContext';

// Constants
const DRAWER_WIDTH = 240;

// Styles
const LAYOUT_STYLES = {
  container: { 
    display: 'flex', 
    width: '100vw', 
    height: '100vh' 
  },
  appBar: { 
    zIndex: (theme: any) => theme.zIndex.drawer + 1 
  },
  appTitle: { 
    cursor: 'pointer' 
  },
  drawer: {
    width: DRAWER_WIDTH,
    flexShrink: 0,
    '& .MuiDrawer-paper': { 
      width: DRAWER_WIDTH, 
      boxSizing: 'border-box' 
    },
  },
  main: { 
    flexGrow: 1, 
    p: 3, 
    width: `calc(100% - ${DRAWER_WIDTH}px)`,
    overflowX: 'auto'
  }
};

interface LayoutProps {
  children: ReactNode;
  onLogout: () => void;
}

export default function Layout({ children, onLogout }: LayoutProps) {
  const navigate = useNavigate();
  const credentials = getCredentials();
  const username = credentials?.username || 'User';
  const { mode, toggleTheme } = useTheme();

  const handleLogout = () => {
    clearCredentials();
    onLogout();
  };

  return (
    <Box sx={{ display: 'flex', width: '100vw', height: '100vh' }}>
      <CssBaseline />
      
      {/* Top App Bar */}
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Typography 
            variant="h6" 
            noWrap 
            component="div" 
            onClick={() => navigate('/')} 
            sx={{ cursor: 'pointer' }}
          >
            Airflow Controller
          </Typography>
          
          {/* User controls */}
          <Stack direction="row" spacing={1} alignItems="center">
            <Tooltip title={`Switch to ${mode === 'dark' ? 'light' : 'dark'} mode`}>
              <IconButton color="inherit" onClick={toggleTheme} size="small">
                {mode === 'dark' ? <Brightness7 /> : <Brightness4 />}
              </IconButton>
            </Tooltip>
            
            <Person fontSize="small" />
            <Typography variant="body2" sx={{ mr: 1 }}>
              {username}
            </Typography>
            
            <Button 
              color="inherit" 
              startIcon={<Logout />}
              onClick={handleLogout}
            >
              Logout
            </Button>
          </Stack>
        </Toolbar>
      </AppBar>
      
      {/* Left Sidebar */}
      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
      >
        <Toolbar /> {/* Spacer to push content below app bar */}
        <Box sx={{ overflow: 'auto' }}>
          {/* Sidebar content goes here */}
        </Box>
      </Drawer>
      
      {/* Main Content Area */}
      <Box component="main" sx={{ 
        flexGrow: 1, 
        p: 3, 
        width: `calc(100% - ${DRAWER_WIDTH}px)`,
        overflowX: 'auto'
      }}>
        <Toolbar /> {/* Spacer to push content below app bar */}
        {children}
      </Box>
    </Box>
  );
} 