import { useState } from 'react';
import { 
  Box, 
  TextField, 
  Button, 
  Typography, 
  Container, 
  Alert, 
  CircularProgress,
  Grid,
  Card,
  CardContent,
  InputAdornment,
  IconButton
} from '@mui/material';
import { saveCredentials, AuthCredentials } from '../utils/auth';
import axios from 'axios';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';
import DnsIcon from '@mui/icons-material/Dns';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';

// Airflow logo URL
const AIRFLOW_LOGO = 'https://airflow.apache.org/images/feature-image.png'; 

// Common input style
const commonInputStyles = {
  mb: 2,
  "& .MuiOutlinedInput-root": {
    backgroundColor: "white"
  },
  "& .MuiInputBase-input": {
    backgroundColor: "white",
    color: "black"
  },
  "& .MuiInputLabel-root": {
    color: "primary.main"
  },
  "& .MuiOutlinedInput-notchedOutline": {
    borderColor: "rgba(0, 0, 0, 0.23)"
  }
};

interface LoginProps {
  onLoginSuccess: () => void;
}

export default function Login({ onLoginSuccess }: LoginProps) {
  const [credentials, setCredentials] = useState<AuthCredentials>({
    username: '',
    password: '',
    serverUrl: 'http://localhost:8008' // Default value
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCredentials(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!credentials.username || !credentials.password || !credentials.serverUrl) {
      setError('Username, password and server URL are required');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Create temporary auth header for authentication
      const tempAuthHeader = `Basic ${btoa(`${credentials.username}:${credentials.password}`)}`;
      
      // Verify credentials with backend
      await axios.post(
        `${credentials.serverUrl}/api/v1/auth/verify`,
        {},
        {
          headers: {
            'Authorization': tempAuthHeader
          }
        }
      );

      // If we reach here, authentication was successful
      saveCredentials(credentials);
      onLoginSuccess();
    } catch (err: any) {
      if (err.response?.status === 401) {
        setError('Invalid username or password');
      } else if (err.code === 'ERR_NETWORK') {
        setError('Could not connect to server. Please check the server URL.');
      } else {
        setError('Login failed. Please try again later.');
      }
      console.error('Login error:', err);
    } finally {
      setLoading(false);
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <Container maxWidth="md" sx={{ 
      height: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center' 
    }}>
      <Card sx={{ 
        width: '100%', 
        borderRadius: 2, 
        boxShadow: '0 8px 24px rgba(0,0,0,0.12)', 
        overflow: 'hidden' 
      }}>
        <Grid container>
          {/* Left side - Logo and slogan */}
          <Grid item xs={12} md={5} sx={{ 
            bgcolor: 'primary.main', 
            color: 'white',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            p: 4
          }}>
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <img 
                src={AIRFLOW_LOGO} 
                alt="Airflow Logo" 
                style={{ 
                  maxWidth: '80%',
                  maxHeight: '80px',
                  objectFit: 'contain'
                }} 
              />
            </Box>
            <Typography variant="h4" component="h1" sx={{ mb: 2, fontWeight: 'bold' }}>
              Airflow Controller
            </Typography>
            <Typography variant="body1">
              Manage and monitor your Airflow pipelines with ease
            </Typography>
          </Grid>
          
          {/* Right side - Login form */}
          <Grid item xs={12} md={7}>
            <CardContent sx={{ p: 4 }}>
              <Typography variant="h5" component="h2" sx={{ mb: 3, fontWeight: 'bold' }}>
                Sign In
              </Typography>
              
              {error && (
                <Alert severity="error" sx={{ mb: 3 }}>
                  {error}
                </Alert>
              )}
              
              <Box component="form" onSubmit={handleSubmit}>
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="serverUrl"
                  label="Server URL"
                  name="serverUrl"
                  autoComplete="url"
                  value={credentials.serverUrl}
                  onChange={handleInputChange}
                  disabled={loading}
                  variant="outlined"
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <DnsIcon color="primary" />
                      </InputAdornment>
                    ),
                  }}
                  sx={commonInputStyles}
                />
                
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="username"
                  label="Username"
                  name="username"
                  autoComplete="username"
                  autoFocus
                  value={credentials.username}
                  onChange={handleInputChange}
                  disabled={loading}
                  variant="outlined"
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <PersonIcon color="primary" />
                      </InputAdornment>
                    ),
                  }}
                  sx={commonInputStyles}
                />
                
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  name="password"
                  label="Password"
                  type={showPassword ? "text" : "password"}
                  id="password"
                  autoComplete="current-password"
                  value={credentials.password}
                  onChange={handleInputChange}
                  disabled={loading}
                  variant="outlined"
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <LockIcon color="primary" />
                      </InputAdornment>
                    ),
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label="toggle password visibility"
                          onClick={togglePasswordVisibility}
                          edge="end"
                        >
                          {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                        </IconButton>
                      </InputAdornment>
                    )
                  }}
                  sx={{...commonInputStyles, mb: 3}}
                />
                
                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  size="large"
                  sx={{ 
                    mt: 2, 
                    py: 1.5,
                    fontSize: '1rem',
                    fontWeight: 'bold'
                  }}
                  disabled={loading}
                >
                  {loading ? <CircularProgress size={24} /> : "Sign In"}
                </Button>
              </Box>
            </CardContent>
          </Grid>
        </Grid>
      </Card>
    </Container>
  );
} 