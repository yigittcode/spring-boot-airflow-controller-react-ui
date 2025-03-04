import { IconButton, Tooltip } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from 'react-router-dom';

interface BackButtonProps {
  to?: string; // Optional specific path to navigate to
}

export default function BackButton({ to }: BackButtonProps) {
  const navigate = useNavigate();
  
  const handleBack = () => {
    if (to) {
      navigate(to);
    } else {
      navigate(-1); // Go back in browser history
    }
  };
  
  return (
    <Tooltip title="Go Back">
      <IconButton 
        onClick={handleBack}
        size="small"
        sx={{ mr: 1 }}
        aria-label="go back"
      >
        <ArrowBackIcon />
      </IconButton>
    </Tooltip>
  );
} 