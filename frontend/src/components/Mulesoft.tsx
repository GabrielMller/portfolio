import { OverridableComponent } from "@mui/material/OverridableComponent";
import SvgIcon, { SvgIconProps, SvgIconTypeMap } from "@mui/material/SvgIcon";

const Mulesoft : OverridableComponent<SvgIconTypeMap<{}, "svg">> & {
    muiName: string;
} = (props : SvgIconProps) => (
  <SvgIcon titleAccess="Mulesoft" {...props} viewBox="0 0 128 128" fill="currentColor">
    <path d="M64,0a64,64,0,1,0,64,64A64.07211,64.07211,0,0,0,64,0ZM86.6,106.82a2.01584,2.01584,0,0,1-1.73.06,2.04249,2.04249,0,0,1-1.13-1.32L79.63,90.18A2.006,2.006,0,0,1,80.45,88,29.98435,29.98435,0,0,0,91.11,50.9L72.28,79.53a2.00755,2.00755,0,0,1-1.67.9H57.38a1.997,1.997,0,0,1-1.68-.91L36.99,50.67A29.98927,29.98927,0,0,0,49.22,89.04,1.98408,1.98408,0,0,1,50.15,91.27L46.18,106.5a1.97657,1.97657,0,0,1-1.08,1.31,1.99618,1.99618,0,0,1-1.7,0A49.3534,49.3534,0,0,1,38.97,20.8a2.1484,2.1484,0,0,1,.19-.13l.67-.39a1.98806,1.98806,0,0,1,2.71.66L64.03,55.55,86.16,21.31a2.0019,2.0019,0,0,1,2.7-.63,49.16959,49.16959,0,0,1,12.85,11.03,1.78973,1.78973,0,0,1,.39.48,49.35556,49.35556,0,0,1-15.5,74.63Z"></path>
  </SvgIcon>
)

Mulesoft.muiName = 'SvgIcon';

export default Mulesoft;