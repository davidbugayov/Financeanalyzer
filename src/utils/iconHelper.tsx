import React from 'react';
import * as Icons from 'lucide-react';

interface IconProps extends React.SVGProps<SVGSVGElement> {
  size?: number | string;
  className?: string;
  color?: string;
}

export const DynamicIcon: React.FC<{ name: string } & IconProps> = ({ name, size = 20, className = '', color, ...props }) => {
  // Try exact name or fallback
  const LucideIcon = (Icons as unknown as Record<string, React.FC<IconProps>>)[name] || Icons.CircleHelp;
  return <LucideIcon size={size} className={className} color={color} {...props} />;
};
