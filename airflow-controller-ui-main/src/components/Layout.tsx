import { Box, CssBaseline, AppBar, Toolbar, Typography, Drawer, Button, Stack, IconButton, Tooltip, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider, Chip } from '@mui/material';
import { Logout, Person, Brightness4, Brightness7, Dashboard, Refresh, History, List as ListIcon } from '@mui/icons-material';
import { ReactNode, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { clearCredentials, getCredentials } from '../utils/auth';
import { formatUserRole } from '../utils/auth';
import { resetApiClient } from '../utils/apiClient';
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
  const location = useLocation();
  const credentials = getCredentials();
  const username = credentials?.username || 'User';
  const userRole = credentials?.role || 'Not assigned';
  const formattedRole = formatUserRole(userRole);
  const { mode, toggleTheme } = useTheme();
  
  // Log user information whenever Layout is rendered
  useEffect(() => {
    console.log('--- LAYOUT USER INFO ---');
    console.log('Current user:', credentials?.username);
    console.log('User role:', credentials?.role);
    console.log('Has token:', !!credentials?.token);
    console.log('Current location:', location.pathname);
    console.log('-------------------------');
  }, [credentials, location.pathname]);

  const handleLogout = () => {
    console.log('Performing logout operations');
    
    // Reset API client first to ensure no more requests go through with old credentials
    resetApiClient();
    
    // Then clear credentials
    clearCredentials();
    
    // Finally notify parent component
    onLogout();
    
    console.log('Logout completed');
  };

  const menuItems = [
    { text: 'DAG List', icon: <Dashboard />, path: '/' },
    { text: 'Activity Logs', icon: <History />, path: '/logs' }
  ];

  // Get appropriate color for role chip
  const getRoleChipColor = (): "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning" => {
    switch(userRole) {
      case 'ADMIN': return 'error';
      case 'OP': return 'warning';
      case 'USER': return 'success';
      case 'VIEWER': return 'info';
      default: return 'default';
    }
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
            
            <Tooltip title="Your current role and permissions">
              <Chip 
                label={formattedRole} 
                size="small" 
                color={getRoleChipColor()}
                variant="outlined"
                sx={{ mr: 1 }}
              />
            </Tooltip>
            
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
          <List>
            {menuItems.map((item) => (
              <ListItem key={item.text} disablePadding>
                <ListItemButton 
                  selected={location.pathname === item.path}
                  onClick={() => navigate(item.path)}
                >
                  <ListItemIcon>
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText primary={item.text} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
          <Divider />
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