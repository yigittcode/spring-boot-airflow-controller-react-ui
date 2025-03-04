import { 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  CircularProgress,
  Box,
  Alert
} from '@mui/material';
import { ReactNode } from 'react';

interface Column<T> {
  id: string;
  label: string;
  render: (item: T, index: number) => ReactNode;
  width?: string | number;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  loading?: boolean;
  error?: string | null;
  keyExtractor: (item: T) => string | number;
  emptyMessage?: string;
}

export default function DataTable<T>({ 
  columns, 
  data, 
  loading = false, 
  error = null,
  keyExtractor,
  emptyMessage = 'No data found' 
}: DataTableProps<T>) {
  return (
    <>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              {columns.map(col => (
                <TableCell key={col.id} width={col.width}>{col.label}</TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 3 }}>
                  <CircularProgress size={24} />
                </TableCell>
              </TableRow>
            ) : data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center">
                  {emptyMessage}
                </TableCell>
              </TableRow>
            ) : (
              data.map((item, index) => (
                <TableRow key={keyExtractor(item)}>
                  {columns.map(col => (
                    <TableCell key={`${keyExtractor(item)}-${col.id}`}>
                      {col.render(item, index)}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
}